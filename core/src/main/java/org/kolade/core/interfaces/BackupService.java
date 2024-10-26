package org.kolade.core.interfaces;

import org.kolade.core.BackupMetadata;
import org.springframework.lang.Nullable;

import java.nio.file.Path;

public interface BackupService {

    Path performFullBackup(String backupDirectory);

    Path performIncrementalBackup(String backupDirectory, @Nullable String archiveDirectory, @Nullable String walArchivePath);

    Path performDifferentialBackup(String backupDirectory);

    void logBackupMetadata(BackupMetadata metadata);
}
