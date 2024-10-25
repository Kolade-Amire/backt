package org.kolade.postgresql;

import org.kolade.core.DatabaseDetails;
import org.kolade.core.exception.CustomBacktException;
import org.kolade.core.exception.DatabaseConnectionException;
import org.kolade.core.interfaces.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class PostgresConnection implements DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(PostgresConnection.class);

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
            return connection != null && !connection.isClosed() && connection.isValid(2);
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

    @Override
    public String getDatabaseName() {
        if (connection != null) {
            try {
                return connection.getCatalog();
            } catch (SQLException e) {
                throw new CustomBacktException("unable to get database name", e);
            }
        }
        return "Unknown";
    }


    @Override
    public String getDatabaseVersion() {
        if (connection != null) {
            try{
                DatabaseMetaData metaData = connection.getMetaData();
                return metaData.getDatabaseProductVersion();
            }catch (SQLException e){
                throw new CustomBacktException("unable to get database version", e);
            }
        }
        return "Unknown";
    }


}
