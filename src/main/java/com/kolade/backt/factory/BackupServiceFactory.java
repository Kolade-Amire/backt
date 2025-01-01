package com.kolade.backt.factory;


import com.kolade.backt.common.DatabaseType;
import com.kolade.backt.service.BackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class BackupServiceFactory {

    private final Map<String, BackupService> backupServiceMap;

    public BackupService getBackupService(String databaseType) {
        if (DatabaseType.isTypeValid(databaseType)) {
            return backupServiceMap.get(databaseType);
        } else {
            throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }
    }
}

