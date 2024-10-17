package org.kolade.core.interfaces;

import org.kolade.core.DatabaseDetails;

import java.sql.SQLException;

public interface DatabaseConnection {


    void connect(DatabaseDetails databaseDetails);

    boolean testConnection();

    void disconnect();

    String getType();

    String getDatabaseName();

    String getDatabaseVersion();

}
