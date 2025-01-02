package com.kolade.backt.mysql;


import com.kolade.backt.common.DatabaseConnection;
import com.kolade.backt.common.DatabaseDetails;
import com.kolade.backt.common.DatabaseType;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component("mysql")
public class MySQLConnection implements DatabaseConnection {

    private Connection connection;
    private DatabaseDetails databaseDetails;

    @Override
    public void connect(DatabaseDetails databaseDetails) throws SQLException {
            connection = DriverManager.getConnection(databaseDetails.getConnectionUrl(), databaseDetails.getUsername(), databaseDetails.getPassword());
            this.databaseDetails = databaseDetails;
    }

    @Override
    public boolean testConnection() throws SQLException {
            return connection != null && !connection.isClosed() && connection.isValid(5);
    }

    @Override
    public void disconnect() throws SQLException {
        if (connection != null) {
            connection.close();
        }

    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.MYSQL;
    }

    @Override
    public DatabaseDetails getDatabaseDetails() {
        return this.databaseDetails;
    }
}
