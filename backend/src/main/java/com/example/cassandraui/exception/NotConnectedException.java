package com.example.cassandraui.exception;

public class NotConnectedException extends RuntimeException {
  private static final String MESSAGE = "Connect to Cassandra before browsing data.";

  public NotConnectedException() {
    super(MESSAGE);
  }
}
