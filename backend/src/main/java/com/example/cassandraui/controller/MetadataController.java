package com.example.cassandraui.controller;

import com.example.cassandraui.dto.KeyspaceDto;
import com.example.cassandraui.dto.KeyspaceRequest;
import com.example.cassandraui.dto.MutationResponse;
import com.example.cassandraui.dto.TableDto;
import com.example.cassandraui.dto.TableSchemaResponse;
import com.example.cassandraui.service.CassandraMetadataService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

  @PostMapping
  public MutationResponse createKeyspace(@Valid @RequestBody KeyspaceRequest request) {
    metadataService.createKeyspace(request.name(), request.replicationFactor());
    return new MutationResponse("Keyspace created.");
  }

  @DeleteMapping("/{keyspace}")
  public MutationResponse dropKeyspace(@PathVariable String keyspace) {
    metadataService.dropKeyspace(keyspace);
    return new MutationResponse("Keyspace dropped.");
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
