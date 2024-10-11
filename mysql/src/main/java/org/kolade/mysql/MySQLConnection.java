package org.kolade.mysql;

import org.kolade.core.exception.DatabaseConnectionException;
import org.kolade.core.interfaces.DatabaseConnection;
import org.kolade.core.DatabaseDetails;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection implements DatabaseConnection {

    private Connection connection;

    @Override
    public void connect(DatabaseDetails databaseDetails) {
        try {
            connection = DriverManager.getConnection(databaseDetails.getConnectionUrl(), databaseDetails.getUsername(), databaseDetails.getPassword());
        } catch (SQLException e) {
            throw new DatabaseConnectionException(e.getMessage(), databaseDetails.getConnectionUrl());
        }
    }

    @Override
    public boolean testConnection() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException("Error closing connection:", e);
            }
        }
    }

    @Override
    public String getType() {
        return "MySQL";
    }
}
