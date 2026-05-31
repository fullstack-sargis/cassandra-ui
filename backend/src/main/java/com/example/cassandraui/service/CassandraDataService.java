package com.example.cassandraui.service;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.example.cassandraui.dto.DataPageResponse;
import com.example.cassandraui.exception.BadRequestException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;

@Service
public class CassandraDataService {
  private static final int FIRST_PAGE = 0;
  private static final int MAX_PAGE_SIZE = 500;
  private static final int MIN_PAGE_SIZE = 1;
  private static final int DEFAULT_QUERY_PAGE_SIZE = 100;
  private static final String PAGE_SIZE_ERROR = "Page size must be greater than zero.";
  private static final String STATEMENT_EXECUTED_MESSAGE = "Statement executed.";
  private static final String SELECT_ALL_FROM = "SELECT * FROM ";
  private static final String CREATE_PREFIX = "create";
  private static final String DROP_PREFIX = "drop";
  private static final String ALTER_PREFIX = "alter";
  private static final String CQL_QUALIFIER = ".";
  private static final String HEX_PREFIX = "0x";
  private static final boolean QUOTE_IDENTIFIERS = true;
  private static final boolean SEQUENTIAL_STREAM = false;

  private final ConnectionService connectionService;
  private final QueryValidator queryValidator;

  public CassandraDataService(ConnectionService connectionService, QueryValidator queryValidator) {
    this.connectionService = connectionService;
    this.queryValidator = queryValidator;
  }

  public DataPageResponse tableData(String keyspace, String table, int page, int size) {
    var pageSize = normalizeSize(size);
    var normalizedPage = Math.max(page, FIRST_PAGE);
    var query = SELECT_ALL_FROM + quote(keyspace) + CQL_QUALIFIER + quote(table);
    var statement = SimpleStatement.builder(query).setPageSize(pageSize).build();
    return executePaged(statement, normalizedPage, pageSize);
  }

  public DataPageResponse query(String query, Integer pageSize) {
    var validated = queryValidator.validateAny(query);
    var normalizedSize = normalizeSize(pageSize == null ? DEFAULT_QUERY_PAGE_SIZE : pageSize);
    var statement = SimpleStatement.builder(validated).setPageSize(normalizedSize).build();
    var session = connectionService.currentSession();
    var resultSet = session.execute(statement);
    if (changesSchema(validated)) {
      session.refreshSchemaAsync().toCompletableFuture().join();
    }
    return resultSet.getColumnDefinitions().size() == 0
        ? new DataPageResponse(
            List.of(), List.of(), FIRST_PAGE, normalizedSize, false, STATEMENT_EXECUTED_MESSAGE)
        : pageFromResultSet(resultSet, FIRST_PAGE, normalizedSize);
  }

  private DataPageResponse executePaged(SimpleStatement statement, int page, int size) {
    var session = connectionService.currentSession();
    var resultSet = session.execute(statement);
    for (var currentPage = FIRST_PAGE;
        currentPage < page && resultSet.getExecutionInfo().getPagingState() != null;
        currentPage++) {
      resultSet =
          session.execute(statement.setPagingState(resultSet.getExecutionInfo().getPagingState()));
    }

    return pageFromResultSet(resultSet, page, size);
  }

  private DataPageResponse pageFromResultSet(ResultSet resultSet, int page, int size) {
    var columns = columnNames(resultSet.getColumnDefinitions());
    var data = rows(resultSet, size).stream().map(row -> rowToMap(row, columns)).toList();

    return new DataPageResponse(
        columns, data, page, size, resultSet.getExecutionInfo().getPagingState() != null);
  }

  private int normalizeSize(int size) {
    if (size < MIN_PAGE_SIZE) {
      throw new BadRequestException(PAGE_SIZE_ERROR);
    }
    return Math.min(size, MAX_PAGE_SIZE);
  }

  private List<String> columnNames(ColumnDefinitions definitions) {
    return StreamSupport.stream(definitions.spliterator(), SEQUENTIAL_STREAM)
        .map(definition -> definition.getName().asInternal())
        .toList();
  }

  private List<Row> rows(ResultSet resultSet, int size) {
    return StreamSupport.stream(resultSet.spliterator(), SEQUENTIAL_STREAM).limit(size).toList();
  }

  private Map<String, Object> rowToMap(Row row, List<String> columns) {
    return IntStream.range(0, columns.size())
        .boxed()
        .collect(
            LinkedHashMap::new,
            (values, index) -> values.put(columns.get(index), formatValue(row.getObject(index))),
            LinkedHashMap::putAll);
  }

  private Object formatValue(Object value) {
    if (value instanceof ByteBuffer buffer) {
      var duplicate = buffer.asReadOnlyBuffer();
      var bytes = new byte[duplicate.remaining()];
      duplicate.get(bytes);
      return HEX_PREFIX + java.util.HexFormat.of().formatHex(bytes);
    }
    if (value instanceof Map<?, ?> map) {
      var formatted = new LinkedHashMap<String, Object>();
      map.forEach((key, mapValue) -> formatted.put(String.valueOf(key), formatValue(mapValue)));
      return formatted;
    }
    if (value instanceof Collection<?> collection) {
      return collection.stream().map(this::formatValue).toList();
    }
    return value;
  }

  private String quote(String identifier) {
    return CqlIdentifier.fromCql(identifier).asCql(QUOTE_IDENTIFIERS);
  }

  private boolean changesSchema(String query) {
    var normalized = query.trim().toLowerCase(Locale.ROOT);
    return normalized.startsWith(CREATE_PREFIX)
        || normalized.startsWith(DROP_PREFIX)
        || normalized.startsWith(ALTER_PREFIX);
  }
}
