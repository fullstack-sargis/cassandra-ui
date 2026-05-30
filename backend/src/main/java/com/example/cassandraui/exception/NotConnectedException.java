package com.example.cassandraui.exception;

public class NotConnectedException extends RuntimeException {
    public NotConnectedException() {
        super("Connect to Cassandra before browsing data.");
    }
}
