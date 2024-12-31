package com.kolade.backt.factory;

import com.kolade.backt.common.DatabaseConnection;
import com.kolade.backt.common.DatabaseType;
import com.kolade.backt.exception.CustomBacktException;
import com.kolade.backt.mongodb.MongoDBConnection;
import com.kolade.backt.mysql.MySQLConnection;
import com.kolade.backt.postgres.PostgresConnection;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;


@Component
public class DatabaseConnectionFactory {

    private final Map<DatabaseType, DatabaseConnection> connectionMap;


    public DatabaseConnectionFactory(MongoDBConnection mongoDBConnection, MySQLConnection mySQLConnection, PostgresConnection postgresConnection) {
        this.connectionMap = new EnumMap<>(DatabaseType.class);

        connectionMap.put(DatabaseType.MONGODB, mongoDBConnection);
        connectionMap.put(DatabaseType.MYSQL, mySQLConnection);
        connectionMap.put(DatabaseType.POSTGRES, postgresConnection);
    }

    public DatabaseConnection getConnection(String databaseType) {
        DatabaseType type = DatabaseType.getTypeByDisplayName(databaseType);
        DatabaseConnection connection = connectionMap.get(type);
        if (connection == null) {
            throw new CustomBacktException("Unsupported database type: " + databaseType);
        }
        return connection;
    }
}
