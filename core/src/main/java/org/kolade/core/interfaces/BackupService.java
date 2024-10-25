package org.kolade.core.interfaces;

import java.nio.file.Path;

public interface BackupService {

    Path performFullBackup() throws Exception;

    Path performIncrementalBackup() throws Exception;

    Path performDifferentialBackup() throws Exception;

    void trackBackupMetadata(Path backupFile, String backupType, String dbName);
}
