package org.kolade.shell;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.kolade.core.DatabaseDetails;
import org.kolade.core.exception.DatabaseConnectionException;
import org.kolade.core.interfaces.DatabaseConnection;
import org.kolade.service.DatabaseConnectionFactory;
import org.kolade.core.DatabaseDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@RequiredArgsConstructor
public class DatabaseConnectionCommands implements DatabaseConnectionCommandsInterface {

    private final DatabaseConnectionFactory databaseConnectionFactory;
    private DatabaseConnection activeConnection;
    private final DatabaseDetailsService databaseDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionCommands.class);

    @PostConstruct
    public void showWelcomeMessage() {

        System.out.println(
                """
                        \nWelcome to Backt!
                        
                        Quick Tutorial:
                        Use 'connect-db' to connect to a database, 'test-db' to check the connection, \n and 'disconnect-db' to disconnect. \n
                        Type 'help' to see available commands.
                        
                        """
        );
    }

    @ShellMethod(value = "Connect to a database\n Example use case: connect-db --type \"postgresql\" --url \"jdbc:postgresql://localhost:5432/dbname\" --username \"postgres\" --password \"postgres\"\n", key = "connect-db")
    @Override
    public String connectToDatabase(@ShellOption(help = "Supported database types are postgres, mysql and mongodb") String type, @ShellOption(help="The JDBC URL or connection string for the database(in case of mongodb)") String url, @ShellOption String username, @ShellOption String password) {
        try {
            var connection = databaseConnectionFactory.getConnection(type);
            var databaseDetails = DatabaseDetails.builder()
                    .connectionUrl(url)
                    .username(username)
                    .password(password)
                    .build();

            connection.connect(databaseDetails);
            activeConnection = connection;
            databaseDetailsService.setActiveDatabaseDetails(databaseDetails);

            logger.info("Successfully connected to the {} database at {}", type, url);
            return "Successfully connected to " + type + " database at " + url;
        } catch (DatabaseConnectionException e) {
            logger.error("Failed to connect to the {} database: {}", type, e.getMessage());
            return "An error occurred while connection to database... Please check the details and try again. " + e.getMessage();
        }
    }

    @ShellMethod(value = "Test the current database connection\n", key = "test-db")
    @Override
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


    @ShellMethod(value = "Disconnect from the current database\n", key = "disconnect-db")
    @Override
    public String disconnectDatabase() {
        if (activeConnection == null) {
            return "No active connection to disconnect.";
        }

        try {
            activeConnection.disconnect();
            databaseDetailsService.clearActiveDatabaseDetails();
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
