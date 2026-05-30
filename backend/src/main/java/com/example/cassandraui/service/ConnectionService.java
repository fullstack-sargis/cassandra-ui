package com.example.cassandraui.service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.example.cassandraui.dto.ConnectionRequest;
import com.example.cassandraui.exception.NotConnectedException;
import java.net.InetSocketAddress;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ConnectionService {
    private volatile CqlSession session;

    public synchronized void connect(ConnectionRequest request) {
        CqlSession newSession = buildSession(request);
        closeCurrentSession();
        this.session = newSession;
    }

    public CqlSession currentSession() {
        CqlSession current = session;
        if (current == null || current.isClosed()) {
            throw new NotConnectedException();
        }
        return current;
    }

    private CqlSession buildSession(ConnectionRequest request) {
        CqlSessionBuilder builder = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(request.host(), request.port()))
                .withLocalDatacenter(request.datacenter());

        Optional.ofNullable(request.username())
                .filter(username -> !username.isBlank())
                .ifPresent(username -> builder.withAuthCredentials(username, Optional.ofNullable(request.password()).orElse("")));

        Optional.ofNullable(request.keyspace())
                .filter(keyspace -> !keyspace.isBlank())
                .ifPresent(builder::withKeyspace);

        return builder.build();
    }

    private void closeCurrentSession() {
        CqlSession current = this.session;
        if (current != null && !current.isClosed()) {
            current.close();
        }
    }
}
