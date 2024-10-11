package org.kolade.postgresql;

import org.kolade.core.interfaces.DatabaseConnection;
import org.kolade.core.DatabaseDetails;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class PostgresConnection implements DatabaseConnection {

    private Connection connection;

    @Override
    public void connect(DatabaseDetails databaseDetails) {
        try {
            connection = DriverManager.getConnection(databaseDetails.getConnectionUrl(), databaseDetails.getUsername(), databaseDetails.getPassword());
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public String getType() {
        return "PostgreSQL";
    }

}
