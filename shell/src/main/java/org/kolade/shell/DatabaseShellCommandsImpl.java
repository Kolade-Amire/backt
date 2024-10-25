package org.kolade.shell;

import lombok.RequiredArgsConstructor;
import org.kolade.core.DatabaseDetails;
import org.kolade.core.exception.DatabaseConnectionException;
import org.kolade.core.interfaces.DatabaseConnection;
import org.kolade.service.factory.DatabaseConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@RequiredArgsConstructor
public class DatabaseShellCommandsImpl{

    private final DatabaseConnectionFactory databaseConnectionFactory;
    private DatabaseConnection activeConnection;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseShellCommandsImpl.class);

    @ShellMethod(value = "Connect to a database", key = "connect-db")
//    @Override
    public String connectToDatabase(@ShellOption String type, @ShellOption String url, @ShellOption String username, @Nullable  @ShellOption String password) {
        try {
            var connection = databaseConnectionFactory.getConnection(type);
            var databaseDetails = DatabaseDetails.builder()
                    .connectionUrl(url)
                    .username(username)
                    .password(password)
                    .build();

            connection.connect(databaseDetails);
            activeConnection = connection;

            logger.info("Successfully connected to the {} database at {}", type, url);
            return "Successfully connected to " + type + " database at " + url;
        } catch (DatabaseConnectionException e) {
            logger.error("Failed to connect to the {} database: {}", type, e.getMessage());
            return "An error occurred while connection to database... Please check the details and try again. " + e.getMessage();
        }
    }

    @ShellMethod("Test the current database connection")
//    @Override
    public String testConnection() {
        if (activeConnection == null) {
            return "No active database connection. Connect to a database";
        }

        try {
            if (activeConnection.testConnection()) {
                logger.info("Connection to the {} is active!.", activeConnection.getType());
                return "Connection to " + activeConnection.getType() + "database is active!";
            } else {
                logger.warn("Connection to {} database is inactive.", activeConnection.getType());
                return "Connection to " + activeConnection.getType() + " database is inactive.";
            }

        } catch (Exception e) {
            logger.error("Error testing connection: {}", e.getMessage());
            return "Error testing the connection: " + e.getMessage();
        }
    }


    @ShellMethod("Disconnect from the current database")
//    @Override
    public String disconnectDatabase() {
        if (activeConnection == null) {
            return "No active connection to disconnect.";
        }

        try {
            activeConnection.disconnect();
            String dbType = activeConnection.getType();
            activeConnection = null;
            logger.info("Successfully disconnected from the {} database.", dbType);
            return "Successfully disconnected from the " + dbType + " database.";
        } catch (Exception e) {
            logger.error("Failed to disconnect from the database: {}", e.getMessage());
            return "Error: Unable to disconnect from the database. " + e.getMessage();
        }
    }
}
