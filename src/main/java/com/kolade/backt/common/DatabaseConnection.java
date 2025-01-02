package com.kolade.backt.common;

import com.kolade.backt.exception.DatabaseConnectionException;

import java.sql.SQLException;

public interface DatabaseConnection {

    void connect (DatabaseDetails databaseDetails) throws SQLException;

    boolean testConnection() throws SQLException;

    void disconnect() throws SQLException;

    DatabaseType getType();

    DatabaseDetails getDatabaseDetails();



}
