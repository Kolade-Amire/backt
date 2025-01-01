package org.kolade.service;

import org.kolade.core.DatabaseType;
import org.kolade.core.exception.CustomBacktException;
import org.kolade.core.interfaces.BackupService;
import org.kolade.mongodb.MongoBackupService;
import org.kolade.mysql.MySQLBackupService;
import org.kolade.postgresql.PostgresBackupService;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class BackupServiceFactory {
    private final Map<DatabaseType, BackupService> backupServiceMap;

    public BackupServiceFactory(
            MySQLBackupService mySQLBackupService,
            PostgresBackupService postgresBackupService,
            MongoBackupService mongoBackupService
    ) {

        this.backupServiceMap = new EnumMap<>(DatabaseType.class);
        backupServiceMap.put(DatabaseType.MYSQL, mySQLBackupService);
        backupServiceMap.put(DatabaseType.POSTGRES, postgresBackupService);
        backupServiceMap.put(DatabaseType.MONGODB, mongoBackupService);
    }

    public BackupService getBackupService(String databaseType) {
        DatabaseType type = DatabaseType.getTypeByDisplayName(databaseType);
        BackupService backupService = backupServiceMap.get(type);
        if (backupService == null) {
            throw new CustomBacktException("Unsupported database type: " + databaseType);
        }

        return backupService;
    }

}
