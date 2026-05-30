package com.example.cassandraui.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.example.cassandraui.dto.ConnectionRequest;
import com.example.cassandraui.exception.NotConnectedException;
import jakarta.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ConnectionService {
  private static final String EMPTY_PASSWORD = "";

  private volatile CqlSession session;

  public synchronized void connect(ConnectionRequest request) {
    var newSession = buildSession(request);
    var previousSession = this.session;
    this.session = newSession;
    closeSession(previousSession);
  }

  public CqlSession currentSession() {
    var current = session;
    if (current == null || current.isClosed()) {
      throw new NotConnectedException();
    }
    return current;
  }

  private CqlSession buildSession(ConnectionRequest request) {
    var builder =
        CqlSession.builder()
            .addContactPoint(new InetSocketAddress(request.host(), request.port()))
            .withLocalDatacenter(request.datacenter());

    Optional.ofNullable(request.username())
        .filter(username -> !username.isBlank())
        .ifPresent(
            username ->
                builder.withAuthCredentials(
                    username, Optional.ofNullable(request.password()).orElse(EMPTY_PASSWORD)));

    Optional.ofNullable(request.keyspace())
        .filter(keyspace -> !keyspace.isBlank())
        .ifPresent(builder::withKeyspace);

    return builder.build();
  }

  @PreDestroy
  public void shutdown() {
    var current = this.session;
    this.session = null;
    closeSession(current);
  }

  private void closeSession(CqlSession session) {
    Optional.ofNullable(session)
        .filter(current -> !current.isClosed())
        .ifPresent(CqlSession::close);
  }
}
