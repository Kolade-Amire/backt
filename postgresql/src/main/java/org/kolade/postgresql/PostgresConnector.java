package org.kolade.postgresql;


import org.kolade.core.interfaces.DatabaseConnector;

import java.sql.Connection;
import java.sql.SQLException;

public class PostgresConnector implements DatabaseConnector {
    @Override
    public Connection connect(String connectionUrl, String username, String password) throws SQLException {
        return null;
    }

    @Override
    public boolean testConnection(Connection connection) throws SQLException {
        return false;
    }

    @Override
    public void disconnect(Connection connection) throws SQLException {

    }
}
