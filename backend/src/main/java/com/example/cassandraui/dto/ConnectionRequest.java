package com.example.cassandraui.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ConnectionRequest(
        @NotBlank String host,
        @Min(1) @Max(65535) int port,
        @NotBlank String datacenter,
        String username,
        String password,
        String keyspace
) {
}
