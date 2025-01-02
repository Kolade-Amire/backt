package com.kolade.backt.exception;

public class DatabaseConnectionException extends RuntimeException {
    private final String connectionUrl;

    public DatabaseConnectionException(Throwable cause, String connectionUrl) {
        super(cause);
        this.connectionUrl = connectionUrl;
    }

    @Override
    public String getMessage() {
        return String.format("Database connection failed: %s (Connection url: %s) %n Root cause: %s", super.getMessage(), connectionUrl, getCause().toString());
    }
}
