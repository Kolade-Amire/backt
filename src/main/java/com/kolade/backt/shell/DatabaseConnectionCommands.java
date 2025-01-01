package com.kolade.backt.shell;

public interface DatabaseConnectionCommands {

    String connectToDatabase(String type, String url, String username, String password, String hostname, int port, String databaseName);

    String testConnection();

    String disconnectDatabase();
}
