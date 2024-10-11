package org.kolade.postgresql;

import org.kolade.core.exception.CustomBacktException;
import org.kolade.core.exception.DatabaseConnectionException;
import org.kolade.core.interfaces.DatabaseConnection;
import org.kolade.core.DatabaseDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class PostgresConnection implements DatabaseConnection {

    Logger logger = LoggerFactory.getLogger(PostgresConnection.class);

    private Connection connection;

    @Override
    public void connect(DatabaseDetails databaseDetails) {
        try {
            connection = DriverManager.getConnection(databaseDetails.getConnectionUrl(), databaseDetails.getUsername(), databaseDetails.getPassword());
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Unable to connect to the PostgreSQL database", e, databaseDetails.getConnectionUrl());
        }
    }

    @Override
    public boolean testConnection() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            logger.error("Error testing connection", e);
            return false;
        }
    }

    @Override
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new CustomBacktException("Unable to close connection", e);
            }
        }

    }

    @Override
    public String getType() {
        return "PostgreSQL";
    }

}
