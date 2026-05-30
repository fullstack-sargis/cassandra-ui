package com.example.cassandraui.service;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.example.cassandraui.dto.DataPageResponse;
import com.example.cassandraui.exception.BadRequestException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CassandraDataService {
    private static final int MAX_PAGE_SIZE = 500;

    private final ConnectionService connectionService;
    private final QueryValidator queryValidator;

    public CassandraDataService(ConnectionService connectionService, QueryValidator queryValidator) {
        this.connectionService = connectionService;
        this.queryValidator = queryValidator;
    }

    public DataPageResponse tableData(String keyspace, String table, int page, int size) {
        int pageSize = normalizeSize(size);
        int normalizedPage = Math.max(page, 0);
        String query = "SELECT * FROM " + quote(keyspace) + "." + quote(table);
        SimpleStatement statement = SimpleStatement.builder(query).setPageSize(pageSize).build();
        return executePaged(statement, normalizedPage, pageSize);
    }

    public DataPageResponse select(String query, Integer pageSize) {
        String validated = queryValidator.validateSelectOnly(query);
        int normalizedSize = normalizeSize(pageSize == null ? 100 : pageSize);
        SimpleStatement statement = SimpleStatement.builder(validated).setPageSize(normalizedSize).build();
        return executePaged(statement, 0, normalizedSize);
    }

    private DataPageResponse executePaged(SimpleStatement statement, int page, int size) {
        CqlSession session = connectionService.currentSession();
        ResultSet resultSet = session.execute(statement);
        for (int currentPage = 0; currentPage < page && resultSet.getExecutionInfo().getPagingState() != null; currentPage++) {
            resultSet = session.execute(statement.setPagingState(resultSet.getExecutionInfo().getPagingState()));
        }

        List<Row> rows = new ArrayList<>();
        int count = 0;
        for (Row row : resultSet) {
            rows.add(row);
            count++;
            if (count >= size) {
                break;
            }
        }

        List<String> columns = columnNames(resultSet.getColumnDefinitions());
        List<Map<String, Object>> data = rows.stream()
                .map(row -> rowToMap(row, columns))
                .toList();

        return new DataPageResponse(columns, data, page, size, resultSet.getExecutionInfo().getPagingState() != null);
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            throw new BadRequestException("Page size must be greater than zero.");
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private List<String> columnNames(ColumnDefinitions definitions) {
        List<String> columns = new ArrayList<>();
        definitions.forEach(definition -> columns.add(definition.getName().asInternal()));
        return columns;
    }

    private Map<String, Object> rowToMap(Row row, List<String> columns) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            Object value = row.getObject(i);
            values.put(columns.get(i), formatValue(value));
        }
        return values;
    }

    private Object formatValue(Object value) {
        if (value instanceof ByteBuffer buffer) {
            ByteBuffer duplicate = buffer.asReadOnlyBuffer();
            byte[] bytes = new byte[duplicate.remaining()];
            duplicate.get(bytes);
            return "0x" + java.util.HexFormat.of().formatHex(bytes);
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> formatted = new LinkedHashMap<>();
            map.forEach((key, mapValue) -> formatted.put(String.valueOf(key), formatValue(mapValue)));
            return formatted;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(this::formatValue).toList();
        }
        if (value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof UUID) {
            return value;
        }
        return value;
    }

    private String quote(String identifier) {
        return CqlIdentifier.fromCql(identifier).asCql(true);
    }
}
