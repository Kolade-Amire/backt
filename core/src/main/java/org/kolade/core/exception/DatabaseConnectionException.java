package org.kolade.core.exception;

public class DatabaseConnectionException extends CustomBacktException {
    private final String connectionUrl;

    public DatabaseConnectionException(String message, String connectionUrl) {
        super(message);
        this.connectionUrl = connectionUrl;
    }

    @Override
    public String getMessage() {
        return String.format("Database connection failed: %s (Connection url: %s)", super.getMessage(), connectionUrl);
    }
}
