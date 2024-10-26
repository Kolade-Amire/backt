package org.kolade.shell;

public interface DatabaseConnectionCommandsInterface {

    String connectToDatabase(String type, String url, String username, String password, String hostname, int port, String databaseName);

    String testConnection();

    String disconnectDatabase();
}
