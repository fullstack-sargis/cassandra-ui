package com.example.cassandraui.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record QueryRequest(
        @NotBlank String query,
        @Min(1) @Max(500) Integer pageSize
) {
}
