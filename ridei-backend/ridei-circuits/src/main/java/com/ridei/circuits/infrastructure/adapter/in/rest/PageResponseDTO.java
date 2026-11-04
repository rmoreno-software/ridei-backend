package com.ridei.circuits.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.function.Function;

import com.ridei.circuits.domain.model.PageResult;

public record PageResponseDTO<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <S, T> PageResponseDTO<T> fromDomain(PageResult<S> result, Function<S, T> mapper) {
        return new PageResponseDTO<>(
            result.content().stream().map(mapper).toList(),
            result.page(),
            result.size(),
            result.totalElements(),
            result.totalPages()
        );
    }
}
