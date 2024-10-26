package org.kolade.mysql;

import lombok.RequiredArgsConstructor;
import org.kolade.core.DatabaseDetails;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public Path performFullBackup() {
        DatabaseDetails databaseDetails = databaseDetailService.getActiveDatabaseDetails();
        if (databaseDetails == null) {
            throw new CustomBacktException("No active database connection");
        }
    }

    @Override
    public Path performIncrementalBackup() {
        return null;
    }

    @Override
    public Path performDifferentialBackup() {
        return null;
    }

    @Override
    public void trackBackupMetadata(Path backupFile, String backupType, String dbName) {

    }

    //To extract database name from connection url
    private String extractDatabaseName(String url) {
        String regex = "//[^/]+/([a-zA-Z0-9_]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException("Invalid URL format: " + url);
        }
    }
}
