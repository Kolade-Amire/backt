package org.kolade.mysql;

import org.kolade.core.exception.CustomBacktException;
import org.kolade.core.interfaces.BackupService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class MySQLBackupService implements BackupService {

    public void executeCommand(String command) throws IOException {

            ProcessBuilder processBuilder = new ProcessBuilder("bin/bash", "-c", command);
            Process process = processBuilder.start();

            try {
                int exitCode = process.waitFor();
                if (exitCode != 0){
                    throw new IOException("Backup process failed with exit code: " + exitCode);
                }
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
                throw new IOException("Backup process was interrupted", e);
            }
    }

    @Override
    public Path performFullBackup() throws Exception {
        return null;
    }

    @Override
    public Path performIncrementalBackup() throws Exception {
        return null;
    }

    @Override
    public Path performDifferentialBackup() throws Exception {
        return null;
    }

    @Override
    public void trackBackupMetadata(Path backupFile, String backupType, String dbName) {

    }
}
