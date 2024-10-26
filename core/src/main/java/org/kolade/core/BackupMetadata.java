package org.kolade.core;

import lombok.Builder;

import java.nio.file.Path;
import java.time.LocalDateTime;

@Builder
public record BackupMetadata(
        String dbType,
        Path backupFilePath,
        BackupType backupType,
        String dbName,
        LocalDateTime timestamp
    ) {
}
