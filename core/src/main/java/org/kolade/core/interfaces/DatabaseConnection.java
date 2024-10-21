package org.kolade.core.interfaces;

import org.kolade.core.DatabaseDetails;
import org.kolade.core.exception.DatabaseConnectionException;

import java.sql.SQLException;

public interface DatabaseConnection {


    void connect(DatabaseDetails databaseDetails) throws DatabaseConnectionException;

    boolean testConnection();

    void disconnect();

    String getType();

    String getDatabaseName();

    String getDatabaseVersion();

}
