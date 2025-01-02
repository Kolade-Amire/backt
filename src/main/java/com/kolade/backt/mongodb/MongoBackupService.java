package com.kolade.backt.mongodb;

import com.kolade.backt.common.BackupMetadata;
import com.kolade.backt.common.BackupType;
import com.kolade.backt.common.DatabaseDetails;
import com.kolade.backt.common.DatabaseType;
import com.kolade.backt.exception.CustomBacktException;
import com.kolade.backt.service.BackupService;
import com.kolade.backt.service.DatabaseDetailsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service("mongodb")
@RequiredArgsConstructor
public class MongoBackupService implements BackupService {

    private static final Logger logger = LoggerFactory.getLogger(MongoBackupService.class);
    private final DatabaseDetailsService databaseDetailsService;


    private void executeCommand(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bin/bash", "-c", command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (
                    BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))
            ) {
                stdOut.lines().forEach(logger::info);  //log standard output
                stdError.lines().forEach(logger::error); //log error output, if any exists

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
     * Performs a full backup of the MongoDB database to the specified directory.
     *
     * @param backupDirectory The directory path where the backup file will be saved
     * @return Path to the backup file
     */
    @Override
    public Path performFullBackup(String backupDirectory) {
        DatabaseDetails databaseDetails = databaseDetailsService.getActiveDatabaseDetails();
        if (databaseDetails == null) {
            throw new CustomBacktException("No active database connection");
        }

        try{
            String backupFilePath = Paths.get(backupDirectory, "/full_backup_" + databaseDetails.getDatabaseName() + "_" + LocalDateTime.now()).toString();

            String command = String.format("mongodump --host %s --port %d --username %s --password %s --db %s --out %s",
                    databaseDetails.getHost(),
                    databaseDetails.getPort(),
                    databaseDetails.getUsername(),
                    databaseDetails.getPassword(),
                    databaseDetails.getDatabaseName(),
                    backupFilePath);

            executeCommand(command);

            Path path = Paths.get(backupFilePath);

            BackupMetadata metadata = BackupMetadata.builder()
                    .dbType(DatabaseType.MONGODB.toString())
                    .backupFilePath(path)
                    .backupType(BackupType.FULL)
                    .dbName(databaseDetails.getDatabaseName())
                    .timestamp(LocalDateTime.now())
                    .build();

            getBackupMetadata(metadata);

            return path;
        } catch (Exception e){
            throw new CustomBacktException("Failed to perform backup operation: ", e);
        }
    }

    @Override
    public Path performIncrementalBackup(String backupDirectory, @Nullable String archiveDirectory, @Nullable String walArchivePath) {
        // MongoDB's incremental backup uses oplog
        Optional<BackupMetadata> lastBackup = metadataRepository
                .findLastSuccessfulBackup(request.databaseName());

        ProcessBuilder pb = new ProcessBuilder(
                "mongodump",
                "--host", connector.getHost(),
                "--port", connector.getPort(),
                "--username", connector.getUsername(),
                "--password", connector.getPassword(),
                "--db", request.databaseName(),
                "--out", tempBackupPath.toString(),
                "--gzip"
        );

        if (lastBackup.isPresent()) {
            // Add oplog replay from last backup
            pb.command().addAll(List.of(
                    "--oplog",
                    "--query", "{\"ts\": {\"$gt\": {\"$timestamp\": {\"t\": " +
                            lastBackup.get().creationTime().toEpochSecond() + ", \"i\": 1}}}}"
            ));
        }

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new BackupException("mongodump failed with exit code: " + exitCode);
        }
    }

    @Override
    public Path performDifferentialBackup(String backupDirectory) {
        // For MongoDB, find last full backup and use oplog from there
        Optional<BackupMetadata> lastFullBackup = metadataRepository
                .findLastFullBackup(request.databaseName());

        if (lastFullBackup.isEmpty()) {
            performFullBackup(request, tempBackupPath);
            return;
        }

        ProcessBuilder pb = new ProcessBuilder(
                "mongodump",
                "--host", connector.getHost(),
                "--port", connector.getPort(),
                "--username", connector.getUsername(),
                "--password", connector.getPassword(),
                "--db", request.databaseName(),
                "--out", tempBackupPath.toString(),
                "--gzip",
                "--oplog",
                "--query", "{\"ts\": {\"$gt\": {\"$timestamp\": {\"t\": " +
                lastFullBackup.get().creationTime().toEpochSecond() + ", \"i\": 1}}}}"
        );

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new BackupException("mongodump failed with exit code: " + exitCode);
        }
    }
    }

    @Override
    public void logBackupMetadata(BackupMetadata metadata) {
        logger.info("Backup created: Type={}, Database_name={}, Path={}, Timestamp: {}", metadata.backupType(), metadata.databaseName(), metadata.backupFilePath().toString(), metadata.timestamp());

    }
}
