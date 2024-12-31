package com.kolade.backt.factory;


import com.kolade.backt.common.DatabaseType;
import com.kolade.backt.exception.CustomBacktException;
import com.kolade.backt.mongodb.MongoBackupService;
import com.kolade.backt.mysql.MySQLBackupService;
import com.kolade.backt.postgres.PostgresBackupService;
import com.kolade.backt.service.BackupService;
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
