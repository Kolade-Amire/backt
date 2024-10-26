package org.kolade.core.interfaces;

import org.kolade.core.BackupMetadata;

import java.nio.file.Path;

public interface BackupService {

    Path performFullBackup(String backupDirectory);

    Path performIncrementalBackup(String backupDirectory);

    Path performDifferentialBackup(String backupDirectory);

    void logBackupMetadata(BackupMetadata metadata);
}
