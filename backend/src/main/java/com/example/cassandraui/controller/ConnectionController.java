package com.example.cassandraui.controller;

import com.example.cassandraui.dto.ConnectionRequest;
import com.example.cassandraui.dto.ConnectionResponse;
import com.example.cassandraui.service.ConnectionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/connections")
public class ConnectionController {
  private final ConnectionService connectionService;

  public ConnectionController(ConnectionService connectionService) {
    this.connectionService = connectionService;
  }

  @PostMapping("/test")
  public ConnectionResponse test(@Valid @RequestBody ConnectionRequest request) {
    connectionService.connect(request);
    return new ConnectionResponse(true, "Connected to Cassandra.");
  }
}
