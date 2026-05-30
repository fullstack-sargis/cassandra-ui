package com.example.cassandraui.dto;

import static com.example.cassandraui.dto.ValidationConstants.MAX_PAGE_SIZE;
import static com.example.cassandraui.dto.ValidationConstants.MIN_PAGE_SIZE;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record QueryRequest(
    @NotBlank String query, @Min(MIN_PAGE_SIZE) @Max(MAX_PAGE_SIZE) Integer pageSize) {}
