package com.vhfantes.fantesday.dto;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

// Pagina de resultados: page comeca em 0; last = true na ultima pagina
public record PageResponseDTO<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last) {

    public static <E, T> PageResponseDTO<T> from(Page<E> page, Function<E, T> mapper) {
        return new PageResponseDTO<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }
}
