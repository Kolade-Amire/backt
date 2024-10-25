package org.kolade.service;

import lombok.RequiredArgsConstructor;
import org.kolade.core.exception.CustomBacktException;
import org.kolade.core.interfaces.DatabaseConnection;
import org.kolade.mongodb.MongoDBConnection;
import org.kolade.mysql.MySQLConnection;
import org.kolade.postgresql.PostgresConnection;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DatabaseConnectionFactory {

    private final MongoDBConnection mongoDBConnection;
    private final MySQLConnection mySQLConnection;
    private final PostgresConnection postgresConnection;


    public DatabaseConnection getConnection(String databaseType) {
        return switch (databaseType.toLowerCase()) {
            case "mongodb" -> mongoDBConnection;
            case "mysql" -> mySQLConnection;
            case "postgres" -> postgresConnection;
            default -> throw new CustomBacktException("Unsupported database type: " + databaseType);
        };
    }
}
