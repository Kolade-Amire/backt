package org.kolade.shell;

public interface DatabaseShellCommands {

    String connectToDatabase(String type, String url, String username, String password);

    String testConnection();

    String disconnectDatabase();
}
