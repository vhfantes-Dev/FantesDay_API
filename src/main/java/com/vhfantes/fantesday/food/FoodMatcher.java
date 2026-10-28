package com.vhfantes.fantesday.food;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.vhfantes.fantesday.entity.CatalogSource;
import com.vhfantes.fantesday.repository.FoodRepository;
import com.vhfantes.fantesday.text.TextNormalizer;

// Acha o alimento do catalogo (TACO + base complementar) mais parecido com um nome livre,
// ex: o nome que a IA deu para um item da foto ("Pizza, calabresa", "Big Mac, McDonald's").
// Compara palavras sem acento, em memoria: o catalogo tem poucas centenas de itens.
@Component
public class FoodMatcher {

    private static final Set<CatalogSource> SOURCES = Set.of(CatalogSource.TACO, CatalogSource.FANTESDAY);

    // Palavras que nao ajudam a diferenciar alimentos
    private static final Set<String> STOPWORDS = Set.of("de", "da", "do", "das", "dos", "com", "e", "a", "o", "em",
            "na", "no", "ao", "tipo");

    // Grafias diferentes do mesmo ingrediente
    private static final Map<String, String> SYNONYMS = Map.of(
            "mucarela", "mussarela",
            "muzarela", "mussarela",
            "mozzarella", "mussarela",
            "hamburger", "hamburguer",
            "burguer", "burger",
            "catupiri", "catupiry",
            "frita", "fritas");

    // Soma das duas coberturas (palavras do nome buscado e do alimento) para aceitar o alimento
    private static final double MIN_SCORE = 1.0;

    private final FoodRepository foodRepository;
    private volatile List<Entry> index;

    public FoodMatcher(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    public Optional<Match> find(String name) {
        Set<String> query = words(name);
        if (query.isEmpty()) {
            return Optional.empty();
        }
        int required = Math.min(2, query.size());

        Entry best = null;
        double bestScore = 0;
        for (Entry entry : index()) {
            long matched = query.stream().filter(entry.words()::contains).count();
            if (matched < required) {
                continue;
            }
            double score = (double) matched / query.size() + (double) matched / entry.words().size();
            if (score > bestScore || (score == bestScore && entry.name().length() < best.name().length())) {
                best = entry;
                bestScore = score;
            }
        }
        return bestScore >= MIN_SCORE ? Optional.of(new Match(best.id(), best.name())) : Optional.empty();
    }

    // Chamado depois da importacao dos catalogos; sem isso, o indice e montado no primeiro uso
    public void reload() {
        index = foodRepository.findBySourceIn(SOURCES).stream()
                .map(food -> new Entry(food.getId(), food.getName(), words(food.getName())))
                .toList();
    }

    private List<Entry> index() {
        if (index == null) {
            reload();
        }
        return index;
    }

    // "Big Mac, McDonald's" -> [big, mac, mcdonalds]
    static Set<String> words(String name) {
        if (name == null) {
            return Set.of();
        }
        return Arrays.stream(TextNormalizer.normalize(name.replace("'", "")).split(" "))
                .filter(word -> !word.isEmpty() && !STOPWORDS.contains(word))
                .map(word -> SYNONYMS.getOrDefault(word, word))
                .collect(Collectors.toSet());
    }

    private record Entry(Long id, String name, Set<String> words) {
    }

    public record Match(Long id, String name) {
    }
}
