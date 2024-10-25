package org.kolade.mysql;

import lombok.RequiredArgsConstructor;
import org.kolade.core.DatabaseDetailsService;
import org.kolade.core.exception.CustomBacktException;
import org.kolade.core.interfaces.BackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MySQLBackupService implements BackupService {

    private static final Logger logger = LoggerFactory.getLogger(MySQLBackupService.class);
    private final DatabaseDetailsService databaseDetailService;

    public void executeCommand(String command) {

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bin/bash", "-c", command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (
                    BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))
            ) {

                //log standard output
                stdOut.lines().forEach(logger::info);

                //log error output (if any)
                stdError.lines().forEach(logger::error);

                boolean isFinished = process.waitFor(300, TimeUnit.SECONDS);
                if (!isFinished) {
                    process.destroyForcibly();
                    throw new IOException("Backup process timed out");
                }
                if (process.exitValue() != 0) {
                    throw new IOException("Backup process failed with exit code: " + process.exitValue());
                }

            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomBacktException("An error occurred during backup process:", e);
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
