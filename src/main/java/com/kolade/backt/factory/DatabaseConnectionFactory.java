package com.kolade.backt.factory;

import com.kolade.backt.common.DatabaseConnection;
import com.kolade.backt.common.DatabaseType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
@RequiredArgsConstructor
public class DatabaseConnectionFactory {

    private final Map<String, DatabaseConnection> connectionMap;

    public DatabaseConnection getConnection(String databaseType) {
        if (DatabaseType.isTypeValid(databaseType)) {
            return connectionMap.get(databaseType);
        } else {
            throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }
    }


}
