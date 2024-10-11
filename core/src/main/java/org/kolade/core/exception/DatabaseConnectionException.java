package org.kolade.core.exception;

public class DatabaseConnectionException extends CustomBacktException {
    private final String connectionUrl;

    public DatabaseConnectionException(String message, Throwable cause, String connectionUrl) {
        super(message, cause);
        this.connectionUrl = connectionUrl;
    }

    @Override
    public String getMessage() {
        return String.format("Database connection failed: %s (Connection url: %s) %n Root cause: %s", super.getMessage(), connectionUrl, getCause().toString());
    }
}
