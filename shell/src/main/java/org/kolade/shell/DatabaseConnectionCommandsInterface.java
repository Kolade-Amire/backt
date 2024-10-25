package org.kolade.shell;

public interface DatabaseConnectionCommandsInterface {

    String connectToDatabase(String type, String url, String username, String password);

    String testConnection();

    String disconnectDatabase();
}
