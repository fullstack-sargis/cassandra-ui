package com.example.cassandraui.dto;

import java.util.List;
import java.util.Map;

public record DataPageResponse(
    List<String> columns, List<Map<String, Object>> rows, int page, int size, boolean hasMore) {}
