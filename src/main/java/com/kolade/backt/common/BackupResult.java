package com.kolade.backt.common;

import lombok.Builder;

import java.nio.file.Path;
import java.time.LocalDateTime;

@Builder
public record BackupResult(
        String backupId,
        BackupType backupType,
        LocalDateTime startTime,
        LocalDateTime endTime,
        long sizeInBytes,
        Path backupFilePath,
        BackupStatus backupStatus,
        String errorMessage
) {}
