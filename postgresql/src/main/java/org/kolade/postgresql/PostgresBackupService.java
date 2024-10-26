package org.kolade.postgresql;

import lombok.RequiredArgsConstructor;
import org.kolade.core.BackupMetadata;
import org.kolade.core.BackupType;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class PostgresBackupService implements BackupService {

    private static final Logger logger = LoggerFactory.getLogger(PostgresConnection.class);
    private final DatabaseDetailsService databaseDetailsService;


    /**
     * Executes a command with password authentication by injecting the password as an environment variable.
     *
     * @param command    The command to execute, formatted for pg_dump
     * @param dbPassword The password for the database
     */
    private void executeCommand(String command, String dbPassword) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bin/bash", "-c", command);
            processBuilder.redirectErrorStream(true);
            processBuilder.environment().put("PGPASSWORD", dbPassword);
            Process process = processBuilder.start();

            // Log output and errors
            try (
                    BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))
            ) {
                stdOut.lines().forEach(logger::info);  //log standard output
                stdError.lines().forEach(logger::error); //log error output, if any

                boolean isFinished = process.waitFor(600, TimeUnit.SECONDS);
                if (!isFinished) {
                    process.destroyForcibly();
                    throw new IOException("Backup process timed out");
                }

                if (process.exitValue() != 0) {
                    throw new IOException("Backup process failed with exit code:" + process.exitValue());
                }
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomBacktException("An error occurred during backup process: ", e);
        }
    }

    /**
     * Performs a full backup of the PostgreSQL database to the specified directory.
     *
     * @param backupDirectory The directory path where the backup file will be saved
     * @return Path to the backup file
     */
    @Override
    public Path performFullBackup(String backupDirectory) {
        DatabaseDetails databaseDetails = databaseDetailsService.getActiveDatabaseDetails();
        if (databaseDetails == null){
            throw new CustomBacktException("No active database connection");
        }

        try{
            String backupFilePath = Paths.get(backupDirectory, "/full_backup_" + databaseDetails.getDbName() + "_" + LocalDateTime.now() + ".sql").toString();
            String command = String.format("pg_dump -h %s -p %d -U %s -d %s -F c -b -v -f %s",
                    databaseDetails.getHost(),
                    databaseDetails.getPort(),
                    databaseDetails.getUsername(),
                    databaseDetails.getDbName(),
                    backupFilePath);

            executeCommand(command, databaseDetails.getPassword());

            Path path = Paths.get(backupFilePath);

            BackupMetadata metadata = BackupMetadata.builder()
                    .backupFilePath(path)
                    .backupType(BackupType.FULL)
                    .dbName(databaseDetails.getDbName())
                    .timestamp(LocalDateTime.now())
                    .build();

            logBackupMetadata(metadata);

            return path;
        } catch (Exception e){
            throw new CustomBacktException("Failed to perform backup operation: ", e);
        }

    }

    @Override
    public Path performIncrementalBackup(String backupDirectory) {
        return null;
    }

    @Override
    public Path performDifferentialBackup(String backupDirectory) {
        return null;
    }

    @Override
    public void logBackupMetadata(BackupMetadata metadata) {
        logger.info("Backup completed. Type: {}, DB: {}, Path: {}, Timestamp: {}", metadata.backupType(), metadata.dbName(), metadata.backupFilePath(), metadata.timestamp());

        String jsonMetadata = String.format("{\"backupType\": \"%s\", \"dbName\": \"%s\", \"filePath\": \"%s\", \"timestamp\": \"%s\"}\n", metadata.backupType(), metadata.dbName(), metadata.backupFilePath().toString(), metadata.timestamp());

        try {
            Files.writeString(Paths.get("backup_metadata.json"), jsonMetadata, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            logger.error("Failed to write backup metadata", e);
        }
    }
}
