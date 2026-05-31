package com.example.cassandraui.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record KeyspaceRequest(@NotBlank String name, @Min(1) @Max(10) Integer replicationFactor) {}
