package com.vhfantes.fantesday.food;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vhfantes.fantesday.entity.Food;
import com.vhfantes.fantesday.repository.FoodRepository;

class FoodMatcherTest {

    private FoodMatcher matcher;

    @BeforeEach
    void setUp() {
        FoodRepository repository = mock(FoodRepository.class);
        when(repository.findBySourceIn(any())).thenReturn(List.of(
                food(1L, "Arroz, tipo 1, cozido"),
                food(2L, "Arroz, integral, cozido"),
                food(3L, "Pizza, calabresa"),
                food(4L, "Pizza, mussarela"),
                food(5L, "Linguiça, porco, calabresa, frita"),
                food(6L, "Big Mac, McDonald's"),
                food(7L, "Batata frita média, McDonald's"),
                food(8L, "Batata, inglesa, frita"),
                food(9L, "Whey protein, concentrado"),
                food(10L, "Feijão, carioca, cozido")));
        matcher = new FoodMatcher(repository);
    }

    @Test
    void findsExactName() {
        assertThat(matcher.find("Arroz, tipo 1, cozido")).map(FoodMatcher.Match::id).contains(1L);
        assertThat(matcher.find("Pizza, calabresa")).map(FoodMatcher.Match::id).contains(3L);
    }

    @Test
    void ignoresAccentsCaseAndSpellingVariants() {
        assertThat(matcher.find("feijao carioca cozido")).map(FoodMatcher.Match::id).contains(10L);
        assertThat(matcher.find("Pizza de muçarela")).map(FoodMatcher.Match::id).contains(4L);
        assertThat(matcher.find("WHEY PROTEIN CONCENTRADO")).map(FoodMatcher.Match::id).contains(9L);
    }

    @Test
    void findsBrandItems() {
        assertThat(matcher.find("Big Mac, McDonald's")).map(FoodMatcher.Match::id).contains(6L);
        assertThat(matcher.find("Hambúrguer Big Mac")).map(FoodMatcher.Match::id).contains(6L);
        assertThat(matcher.find("Batata frita média, McDonald's")).map(FoodMatcher.Match::id).contains(7L);
    }

    @Test
    void prefersTheClosestName() {
        // "calabresa" sozinha nao basta: a pizza ganha da linguica
        assertThat(matcher.find("Pizza calabresa")).map(FoodMatcher.Match::id).contains(3L);
        assertThat(matcher.find("Batata frita")).map(FoodMatcher.Match::id).contains(8L);
    }

    @Test
    void returnsEmptyWhenNothingIsCloseEnough() {
        assertThat(matcher.find("Pizza, frango com catupiry")).isEmpty();
        assertThat(matcher.find("Lasanha, bolonhesa")).isEmpty();
        assertThat(matcher.find(null)).isEmpty();
        assertThat(matcher.find(" ")).isEmpty();
    }

    private static Food food(Long id, String name) {
        Food food = new Food();
        food.setId(id);
        food.setName(name);
        return food;
    }
}
