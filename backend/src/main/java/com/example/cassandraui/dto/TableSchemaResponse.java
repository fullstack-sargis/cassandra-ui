package com.example.cassandraui.dto;

import java.util.List;

public record TableSchemaResponse(
        String keyspace,
        String table,
        List<ColumnDto> columns,
        String createStatement
) {
}
