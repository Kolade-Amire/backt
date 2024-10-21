package org.kolade.shell;

import lombok.RequiredArgsConstructor;
import org.kolade.core.DatabaseDetails;
import org.kolade.core.exception.DatabaseConnectionException;
import org.kolade.core.interfaces.DatabaseConnection;
import org.kolade.service.factory.DatabaseConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@RequiredArgsConstructor
public class DatabaseShellCommandsImpl implements DatabaseShellCommands {

    private final DatabaseConnectionFactory databaseConnectionFactory;
    private DatabaseConnection activeConnection;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseShellCommandsImpl.class);

    @ShellMethod("Connect to a database")
    @Override
    public String connectToDatabase(@ShellOption String type, @ShellOption String url, @ShellOption String username, @ShellOption String password) {
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
    @Override
    public String testConnection() {
        return "";
    }

    @Override
    public String disconnectDatabase() {
        return "";
    }
}
