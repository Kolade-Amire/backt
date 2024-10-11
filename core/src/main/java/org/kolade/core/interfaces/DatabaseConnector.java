package org.kolade.core.interfaces;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseConnector {

    // Establishes a connection to the database
    Connection connect(String connectionUrl, String username, String password) throws SQLException;

    // Tests if the connection to the database is valid
    boolean testConnection(Connection connection) throws SQLException;

    // Closes the connection to the database
    void disconnect(Connection connection) throws SQLException;
}
