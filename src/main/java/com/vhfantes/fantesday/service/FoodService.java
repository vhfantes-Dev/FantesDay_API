package com.vhfantes.fantesday.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import com.vhfantes.fantesday.client.OpenFoodFactsClient;
import com.vhfantes.fantesday.client.dto.OffNutriments;
import com.vhfantes.fantesday.client.dto.OffProduct;
import com.vhfantes.fantesday.dto.ExternalFoodDTO;
import com.vhfantes.fantesday.dto.FoodRequestDTO;
import com.vhfantes.fantesday.dto.FoodResponseDTO;
import com.vhfantes.fantesday.dto.PageResponseDTO;
import com.vhfantes.fantesday.entity.CatalogSource;
import com.vhfantes.fantesday.entity.Food;
import com.vhfantes.fantesday.exception.BusinessException;
import com.vhfantes.fantesday.exception.ForbiddenException;
import com.vhfantes.fantesday.exception.ResourceNotFoundException;
import com.vhfantes.fantesday.food.FoodUnits;
import com.vhfantes.fantesday.repository.FoodRepository;
import com.vhfantes.fantesday.repository.MealItemRepository;
import com.vhfantes.fantesday.security.CurrentUser;
import com.vhfantes.fantesday.text.TextNormalizer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodService {

    private static final Logger logger = LoggerFactory.getLogger(FoodService.class);

    // Limite das colunas numeric(7,2); valores acima disso no Open Food Facts sao dados errados
    private static final BigDecimal MAX_NUTRIENT_VALUE = BigDecimal.valueOf(100_000);

    // Quantos alimentos com a primeira palavra do nome sao comparados no findBestMatch
    private static final int MATCH_CANDIDATES = 100;

    private final FoodRepository foodRepository;
    private final MealItemRepository mealItemRepository;
    private final OpenFoodFactsClient openFoodFactsClient;
    private final UserService userService;
    private final CurrentUser currentUser;

    // Lista paginada; com name, pesquisa por parte do nome (sem diferenciar maiusculas/minusculas)
    @Transactional(readOnly = true)
    public PageResponseDTO<FoodResponseDTO> search(String name, Pageable pageable) {
        Page<Food> page = name != null && !name.isBlank()
                ? foodRepository.findByNameContainingIgnoreCase(name.trim(), pageable)
                : foodRepository.findAll(pageable);
        return PageResponseDTO.from(page, FoodResponseDTO::from);
    }

    @Transactional(readOnly = true)
    public FoodResponseDTO findById(Long id) {
        return FoodResponseDTO.from(getFoodOrThrow(id));
    }

    @Transactional
    public FoodResponseDTO create(FoodRequestDTO dto) {
        if (dto.name() == null || dto.name().isBlank()) {
            throw new BusinessException("Nome e obrigatorio");
        }

        Food food = new Food();
        food.setName(dto.name().trim());
        food.setCaloriesPer100g(dto.caloriesPer100g());
        food.setProteinPer100g(dto.proteinPer100g());
        food.setCarbsPer100g(dto.carbsPer100g());
        food.setFatPer100g(dto.fatPer100g());
        food.setFiberPer100g(dto.fiberPer100g());
        boolean liquid = Boolean.TRUE.equals(dto.liquid());
        food.setLiquid(liquid);
        food.setDensityGPerMl(validateDensity(dto.densityGPerMl()));
        food.setDefaultUnit(dto.defaultUnit() != null
                ? FoodUnits.normalize(dto.defaultUnit(), null)
                : (liquid ? FoodUnits.MILLILITERS : FoodUnits.GRAMS));
        food.setSource(CatalogSource.CUSTOM);
        food.setCreatedBy(userService.getUserOrThrow(currentUser.id()));

        return FoodResponseDTO.from(foodRepository.save(food));
    }

    @Transactional
    public FoodResponseDTO update(Long id, FoodRequestDTO dto) {
        Food food = getEditableFoodOrThrow(id);

        if (dto.name() != null && !dto.name().isBlank()) {
            food.setName(dto.name().trim());
        }
        if (dto.caloriesPer100g() != null) {
            food.setCaloriesPer100g(dto.caloriesPer100g());
        }
        if (dto.proteinPer100g() != null) {
            food.setProteinPer100g(dto.proteinPer100g());
        }
        if (dto.carbsPer100g() != null) {
            food.setCarbsPer100g(dto.carbsPer100g());
        }
        if (dto.fatPer100g() != null) {
            food.setFatPer100g(dto.fatPer100g());
        }
        if (dto.fiberPer100g() != null) {
            food.setFiberPer100g(dto.fiberPer100g());
        }
        if (dto.liquid() != null) {
            food.setLiquid(dto.liquid());
        }
        if (dto.densityGPerMl() != null) {
            food.setDensityGPerMl(validateDensity(dto.densityGPerMl()));
        }
        if (dto.defaultUnit() != null) {
            food.setDefaultUnit(FoodUnits.normalize(dto.defaultUnit(), null));
        }

        return FoodResponseDTO.from(food);
    }

    @Transactional
    public void delete(Long id) {
        Food food = getEditableFoodOrThrow(id);
        if (mealItemRepository.existsByFoodId(id)) {
            throw new BusinessException("Alimento usado em refeicoes; remova-o das refeicoes antes de excluir");
        }
        foodRepository.delete(food);
    }

    // Alimentos importados (TACO e Open Food Facts) sao compartilhados e nao mudam;
    // os cadastrados manualmente so podem ser alterados por quem os criou
    private Food getEditableFoodOrThrow(Long id) {
        Food food = getFoodOrThrow(id);
        if (food.getSource() != null && food.getSource() != CatalogSource.CUSTOM) {
            throw new ForbiddenException("Alimentos do catalogo (" + food.getSource() + ") nao podem ser alterados");
        }
        if (food.getCreatedBy() == null || !food.getCreatedBy().getId().equals(currentUser.id())) {
            throw new ForbiddenException("Apenas quem cadastrou o alimento pode altera-lo");
        }
        return food;
    }

    // ---------- Open Food Facts ----------

    // Procura primeiro no banco; se nao achar, busca no Open Food Facts e salva (cache local)
    @Transactional
    public FoodResponseDTO findOrImportByBarcode(String barcode) {
        if (barcode == null || barcode.isBlank()) {
            throw new BusinessException("Codigo de barras e obrigatorio");
        }
        String code = barcode.trim();

        return foodRepository.findBySourceAndExternalId(CatalogSource.OPEN_FOOD_FACTS, code)
                .map(FoodResponseDTO::from)
                .orElseGet(() -> FoodResponseDTO.from(foodRepository.save(importFromOpenFoodFacts(code))));
    }

    // Busca produtos no Open Food Facts sem salvar. Se o servico falhar, devolve lista vazia.
    public List<ExternalFoodDTO> searchExternal(String term) {
        if (term == null || term.isBlank()) {
            return List.of();
        }

        try {
            return openFoodFactsClient.search(term.trim()).stream()
                    .filter(product -> product.code() != null && product.displayName() != null)
                    .map(this::toExternalFood)
                    .toList();
        } catch (RestClientException e) {
            logger.warn("Falha ao buscar '{}' no Open Food Facts: {}", term, e.getMessage());
            return List.of();
        }
    }

    // Alimento do catalogo mais parecido com o nome (no padrao da TACO, ex: "Arroz, tipo 1, cozido").
    // Busca pela primeira palavra e fica com o que tiver mais palavras em comum (empate: o nome mais curto).
    @Transactional(readOnly = true)
    public Optional<Food> findBestMatch(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        List<String> words = Arrays.stream(TextNormalizer.normalize(name).split(" "))
                .filter(word -> !word.isEmpty())
                .toList();
        if (words.isEmpty()) {
            return Optional.empty();
        }
        String firstWord = name.trim().split("[\\s,]+")[0];
        int requiredScore = Math.min(2, words.size());

        Food best = null;
        int bestScore = 0;
        for (Food food : foodRepository.findByNameContainingIgnoreCase(firstWord, PageRequest.of(0, MATCH_CANDIDATES))) {
            Set<String> foodWords = new HashSet<>(Arrays.asList(TextNormalizer.normalize(food.getName()).split(" ")));
            int score = (int) words.stream().filter(foodWords::contains).count();
            if (score < requiredScore) {
                continue;
            }
            if (score > bestScore || (score == bestScore && food.getName().length() < best.getName().length())) {
                best = food;
                bestScore = score;
            }
        }
        return Optional.ofNullable(best);
    }

    // Usado pelo MealService para montar os itens de uma refeicao
    @Transactional(readOnly = true)
    public Food getFoodOrThrow(Long id) {
        return foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alimento nao encontrado: " + id));
    }

    private Food importFromOpenFoodFacts(String barcode) {
        OffProduct product;
        try {
            product = openFoodFactsClient.findByBarcode(barcode)
                    .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado: " + barcode));
        } catch (RestClientException e) {
            logger.warn("Falha ao consultar o codigo {} no Open Food Facts: {}", barcode, e.getMessage());
            throw new BusinessException("Servico de alimentos indisponivel");
        }

        OffNutriments nutriments = product.nutriments();

        Food food = new Food();
        food.setName(product.displayName() != null ? product.displayName() : "Produto " + barcode);
        if (nutriments != null) {
            food.setCaloriesPer100g(sanitize(nutriments.energyKcal100g()));
            food.setProteinPer100g(sanitize(nutriments.proteins100g()));
            food.setCarbsPer100g(sanitize(nutriments.carbohydrates100g()));
            food.setFatPer100g(sanitize(nutriments.fat100g()));
            food.setFiberPer100g(sanitize(nutriments.fiber100g()));
        }
        food.setLiquid(product.isLiquid());
        food.setDefaultUnit(product.isLiquid() ? FoodUnits.MILLILITERS : FoodUnits.GRAMS);
        food.setSource(CatalogSource.OPEN_FOOD_FACTS);
        food.setExternalId(barcode);
        return food;
    }

    private ExternalFoodDTO toExternalFood(OffProduct product) {
        OffNutriments nutriments = product.nutriments();
        if (nutriments == null) {
            return new ExternalFoodDTO(product.code(), product.displayName(), product.brands(),
                    null, null, null, null, null);
        }
        return new ExternalFoodDTO(
                product.code(),
                product.displayName(),
                product.brands(),
                sanitize(nutriments.energyKcal100g()),
                sanitize(nutriments.proteins100g()),
                sanitize(nutriments.carbohydrates100g()),
                sanitize(nutriments.fat100g()),
                sanitize(nutriments.fiber100g()));
    }

    // Descarta valores negativos ou absurdos e arredonda para 2 casas
    private BigDecimal sanitize(BigDecimal value) {
        if (value == null || value.signum() < 0 || value.compareTo(MAX_NUTRIENT_VALUE) >= 0) {
            return null;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    // Densidade entre 0,5 e 2 g/ml cobre os alimentos liquidos reais (oleos ~0,9; mel ~1,4)
    private BigDecimal validateDensity(BigDecimal density) {
        if (density == null) {
            return null;
        }
        if (density.compareTo(new BigDecimal("0.5")) < 0 || density.compareTo(new BigDecimal("2")) > 0) {
            throw new BusinessException("Densidade deve estar entre 0,5 e 2 g/ml");
        }
        return density;
    }
}
