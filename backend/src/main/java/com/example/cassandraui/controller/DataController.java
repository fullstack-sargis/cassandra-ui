package com.example.cassandraui.controller;

import com.example.cassandraui.dto.DataPageResponse;
import com.example.cassandraui.dto.QueryRequest;
import com.example.cassandraui.service.CassandraDataService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataController {
  private final CassandraDataService dataService;

  public DataController(CassandraDataService dataService) {
    this.dataService = dataService;
  }

  @GetMapping("/api/keyspaces/{keyspace}/tables/{table}/data")
  public DataPageResponse tableData(
      @PathVariable String keyspace,
      @PathVariable String table,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {
    return dataService.tableData(keyspace, table, page, size);
  }

  @PostMapping("/api/query")
  public DataPageResponse query(@Valid @RequestBody QueryRequest request) {
    return dataService.select(request.query(), request.pageSize());
  }
}
