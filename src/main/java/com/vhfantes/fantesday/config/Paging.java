package com.vhfantes.fantesday.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.vhfantes.fantesday.exception.BusinessException;

// Monta o Pageable a partir de ?page=&size= com tamanho padrao e maximo (app.pagination.*)
@Component
public class Paging {

    private final int defaultSize;
    private final int maxSize;

    public Paging(@Value("${app.pagination.default-size}") int defaultSize,
            @Value("${app.pagination.max-size}") int maxSize) {
        this.defaultSize = defaultSize;
        this.maxSize = maxSize;
    }

    public Pageable of(Integer page, Integer size, Sort sort) {
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : defaultSize;
        if (pageNumber < 0) {
            throw new BusinessException("page deve ser 0 ou maior");
        }
        if (pageSize < 1 || pageSize > maxSize) {
            throw new BusinessException("size deve estar entre 1 e " + maxSize);
        }
        return PageRequest.of(pageNumber, pageSize, sort);
    }
}
