package com.example.cassandraui.dto;

import static com.example.cassandraui.dto.ValidationConstants.MAX_PORT;
import static com.example.cassandraui.dto.ValidationConstants.MIN_PORT;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ConnectionRequest(
    @NotBlank String host,
    @Min(MIN_PORT) @Max(MAX_PORT) int port,
    @NotBlank String datacenter,
    String username,
    String password,
    String keyspace) {}
