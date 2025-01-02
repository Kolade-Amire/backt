package com.kolade.backt.common;

import java.nio.file.Path;
import java.util.Map;

public record BackupRequest(
        String databaseName,
        BackupType backupType,
        Path destinationPath,
        boolean compress,
        Map<String, String> otherOptions
) {
}
