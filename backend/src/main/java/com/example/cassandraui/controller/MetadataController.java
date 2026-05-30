package com.example.cassandraui.controller;

import com.example.cassandraui.dto.KeyspaceDto;
import com.example.cassandraui.dto.TableDto;
import com.example.cassandraui.dto.TableSchemaResponse;
import com.example.cassandraui.service.CassandraMetadataService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/keyspaces")
public class MetadataController {
    private final CassandraMetadataService metadataService;

    public MetadataController(CassandraMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping
    public List<KeyspaceDto> keyspaces() {
        return metadataService.keyspaces();
    }

    @GetMapping("/{keyspace}/tables")
    public List<TableDto> tables(@PathVariable String keyspace) {
        return metadataService.tables(keyspace);
    }

    @GetMapping("/{keyspace}/tables/{table}/schema")
    public TableSchemaResponse schema(@PathVariable String keyspace, @PathVariable String table) {
        return metadataService.schema(keyspace, table);
    }
}
