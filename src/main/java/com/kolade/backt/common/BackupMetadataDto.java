package com.kolade.backt.common;

import lombok.Builder;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record BackupMetadataDto(
        String id,
        DatabaseType databaseType,
        Path backupFilePath,
        BackupType backupType,
        String databaseName,
        LocalDateTime creationTime,
        Map<String, String> additionalInfo
    ) {
}