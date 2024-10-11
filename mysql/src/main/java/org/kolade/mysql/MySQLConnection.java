package org.kolade.mysql;

import org.kolade.core.exception.CustomBacktException;
import org.kolade.core.exception.DatabaseConnectionException;
import org.kolade.core.interfaces.DatabaseConnection;
import org.kolade.core.DatabaseDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection implements DatabaseConnection {

    Logger logger = LoggerFactory.getLogger(MySQLConnection.class);

    private Connection connection;

    @Override
    public void connect(DatabaseDetails databaseDetails) {
        try {
            connection = DriverManager.getConnection(databaseDetails.getConnectionUrl(), databaseDetails.getUsername(), databaseDetails.getPassword());
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Unable to connect to the MySQL database", e, databaseDetails.getConnectionUrl());
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
        return "MySQL";
    }
}
