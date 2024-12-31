package com.kolade.backt.common;

import com.kolade.backt.exception.DatabaseConnectionException;

public interface DatabaseConnection {


    void connect(DatabaseDetails databaseDetails) throws DatabaseConnectionException;

    boolean testConnection();

    void disconnect();

    String getType();

    String getDatabaseName();

    String getDatabaseVersion();



}
