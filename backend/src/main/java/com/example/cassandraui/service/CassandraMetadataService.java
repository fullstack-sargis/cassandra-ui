package com.example.cassandraui.service;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.example.cassandraui.dto.ColumnDto;
import com.example.cassandraui.dto.KeyspaceDto;
import com.example.cassandraui.dto.TableDto;
import com.example.cassandraui.dto.TableSchemaResponse;
import com.example.cassandraui.exception.BadRequestException;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CassandraMetadataService {
  private static final boolean INCLUDE_INTERNAL_DETAILS = false;
  private static final boolean INCLUDE_FROZEN_TYPES = true;
  private static final String CLUSTERING_COLUMN = "clustering";
  private static final String KEYSPACE_NOT_FOUND = "Keyspace not found: ";
  private static final String PARTITION_KEY = "partition_key";
  private static final String REGULAR_COLUMN = "regular";
  private static final String SYSTEM_KEYSPACE_PREFIX = "system";
  private static final String TABLE_NOT_FOUND = "Table not found: ";
  private static final String TABLE_QUALIFIER = ".";

  private final ConnectionService connectionService;

  public CassandraMetadataService(ConnectionService connectionService) {
    this.connectionService = connectionService;
  }

  public List<KeyspaceDto> keyspaces() {
    return connectionService.currentSession().getMetadata().getKeyspaces().keySet().stream()
        .map(CqlIdentifier::asInternal)
        .sorted()
        .map(name -> new KeyspaceDto(name, name.startsWith(SYSTEM_KEYSPACE_PREFIX)))
        .toList();
  }

  public List<TableDto> tables(String keyspace) {
    return keyspaceMetadata(connectionService.currentSession(), keyspace)
        .getTables()
        .keySet()
        .stream()
        .map(CqlIdentifier::asInternal)
        .sorted()
        .map(TableDto::new)
        .toList();
  }

  public TableSchemaResponse schema(String keyspace, String table) {
    var metadata = tableMetadata(connectionService.currentSession(), keyspace, table);
    var columns =
        metadata.getColumns().values().stream()
            .map(
                column ->
                    new ColumnDto(
                        column.getName().asInternal(),
                        column.getType().asCql(INCLUDE_INTERNAL_DETAILS, INCLUDE_FROZEN_TYPES),
                        metadata.getPartitionKey().contains(column)
                            ? PARTITION_KEY
                            : metadata.getClusteringColumns().containsKey(column)
                                ? CLUSTERING_COLUMN
                                : REGULAR_COLUMN))
            .sorted(Comparator.comparing(ColumnDto::kind).thenComparing(ColumnDto::name))
            .toList();

    return new TableSchemaResponse(
        keyspace, table, columns, metadata.describe(INCLUDE_INTERNAL_DETAILS));
  }

  private KeyspaceMetadata keyspaceMetadata(CqlSession session, String keyspace) {
    return session
        .getMetadata()
        .getKeyspace(CqlIdentifier.fromCql(keyspace))
        .orElseThrow(() -> new BadRequestException(KEYSPACE_NOT_FOUND + keyspace));
  }

  private TableMetadata tableMetadata(CqlSession session, String keyspace, String table) {
    return keyspaceMetadata(session, keyspace)
        .getTable(CqlIdentifier.fromCql(table))
        .orElseThrow(
            () -> new BadRequestException(TABLE_NOT_FOUND + keyspace + TABLE_QUALIFIER + table));
  }
}
