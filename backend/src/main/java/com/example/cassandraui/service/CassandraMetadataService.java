package com.example.cassandraui.service;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.metadata.schema.ColumnMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.example.cassandraui.dto.ColumnDto;
import com.example.cassandraui.dto.KeyspaceDto;
import com.example.cassandraui.dto.TableDto;
import com.example.cassandraui.dto.TableSchemaResponse;
import com.example.cassandraui.exception.BadRequestException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CassandraMetadataService {
    private final ConnectionService connectionService;

    public CassandraMetadataService(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    public List<KeyspaceDto> keyspaces() {
        return connectionService.currentSession().getMetadata().getKeyspaces().keySet().stream()
                .map(CqlIdentifier::asInternal)
                .sorted()
                .map(name -> new KeyspaceDto(name, name.startsWith("system")))
                .toList();
    }

    public List<TableDto> tables(String keyspace) {
        return keyspaceMetadata(connectionService.currentSession(), keyspace).getTables().keySet().stream()
                .map(CqlIdentifier::asInternal)
                .sorted()
                .map(TableDto::new)
                .toList();
    }

    public TableSchemaResponse schema(String keyspace, String table) {
        TableMetadata metadata = tableMetadata(connectionService.currentSession(), keyspace, table);
        Map<CqlIdentifier, ColumnMetadata> columnsByName = metadata.getColumns();
        List<ColumnDto> columns = columnsByName.values().stream()
                .map(column -> new ColumnDto(
                        column.getName().asInternal(),
                        column.getType().asCql(false, true),
                        metadata.getPartitionKey().contains(column)
                                ? "partition_key"
                                : metadata.getClusteringColumns().containsKey(column) ? "clustering" : "regular"))
                .sorted(Comparator.comparing(ColumnDto::kind).thenComparing(ColumnDto::name))
                .toList();

        return new TableSchemaResponse(
                keyspace,
                table,
                columns,
                metadata.describe(false));
    }

    private KeyspaceMetadata keyspaceMetadata(CqlSession session, String keyspace) {
        return session.getMetadata()
                .getKeyspace(CqlIdentifier.fromCql(keyspace))
                .orElseThrow(() -> new BadRequestException("Keyspace not found: " + keyspace));
    }

    private TableMetadata tableMetadata(CqlSession session, String keyspace, String table) {
        return keyspaceMetadata(session, keyspace)
                .getTable(CqlIdentifier.fromCql(table))
                .orElseThrow(() -> new BadRequestException("Table not found: " + keyspace + "." + table));
    }
}
