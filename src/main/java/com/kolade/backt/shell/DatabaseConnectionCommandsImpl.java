package com.kolade.backt.shell;

import com.kolade.backt.common.DatabaseConnection;
import com.kolade.backt.common.DatabaseDetails;
import com.kolade.backt.common.DatabaseType;
import com.kolade.backt.exception.DatabaseConnectionException;
import com.kolade.backt.factory.DatabaseConnectionFactory;
import com.kolade.backt.service.DatabaseDetailsService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.sql.SQLException;

@ShellComponent
@RequiredArgsConstructor
public class DatabaseConnectionCommandsImpl implements DatabaseConnectionCommands {

    private final DatabaseConnectionFactory databaseConnectionFactory;
    private DatabaseConnection activeConnection;
    private final DatabaseDetailsService databaseDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionCommandsImpl.class);

    @PostConstruct
    public void showWelcomeMessage() {

        System.out.println(
                """
                        \nWelcome to Backt!
                        
                        Quick Tutorial:
                        Use 'connect-db' to connect to a database, 'test-db' to check the connection, \n and 'disconnect-db' to disconnect. \n
                        Type 'help' to see available commands.
                        --hostname "localhost" --port 3306 --dbname "demoDatabase"
                        """
        );
    }

    @ShellMethod(value = "Connect to a database\n Example use case: connect-db --type \"postgresql\" --url \"jdbc:postgresql://localhost:5432/dbname\" --username \"postgres\" --password \"postgres\"  --host \"localhost\" --port 3306 --dbname \"demoDatabase\"\n ", key = "connect-db")
    @Override
    public String connectToDatabase(
            @ShellOption(help = "Supported database types are postgres, mysql and mongodb --- use the names as written") String type,
            @ShellOption(help = "The JDBC URL or connection string for the database(in case of mongodb)") String url,
            @ShellOption(help = "username for database login") String username,
            @ShellOption(help = "password for database login") String password,
            @ShellOption(help = "database host name (could also be the host IP address", defaultValue = "localhost") String host,
            @ShellOption(help = "database port") int port,
            @ShellOption(help = "name of database") String dbname
    ) {
        try {

            var connection = databaseConnectionFactory.getConnection(type);
            var databaseDetails = DatabaseDetails.builder()
                    .connectionUrl(url)
                    .username(username)
                    .password(password)
                    .host(host)
                    .port(port)
                    .databaseName(dbname)
                    .build();

            connection.connect(databaseDetails);
            activeConnection = connection;
            databaseDetailsService.setActiveDatabaseDetails(databaseDetails);
            databaseDetailsService.setActiveDatabaseConnection(activeConnection);

            logger.info("Successfully connected to the {} database at {}", type, url);
            return "Successfully connected to " + type + " database at " + url;
        } catch (SQLException | DatabaseConnectionException e) {
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
            databaseDetailsService.clearActiveDatabase();
            DatabaseType dbType = activeConnection.getType();
            activeConnection = null;
            logger.info("Successfully disconnected from the {} database.", dbType);
            return "Successfully disconnected from the " + dbType + " database.";
        } catch (Exception e) {
            logger.error("Failed to disconnect from the database: {}", e.getMessage());
            return "Error: Unable to disconnect from the database. " + e.getMessage();
        }
    }
}
