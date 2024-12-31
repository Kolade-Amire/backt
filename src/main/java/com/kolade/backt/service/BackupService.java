package com.kolade.backt.service;

import com.kolade.backt.common.BackupMetadata;
import org.springframework.lang.Nullable;

import java.nio.file.Path;

public interface BackupService {

    Path performFullBackup(String backupDirectory);

    Path performIncrementalBackup(String backupDirectory, @Nullable String archiveDirectory, @Nullable String walArchivePath);

    Path performDifferentialBackup(String backupDirectory);

    void logBackupMetadata(BackupMetadata metadata);
}
