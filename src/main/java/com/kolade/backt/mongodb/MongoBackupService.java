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
import java.util.concurrent.TimeUnit;

@Service
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
            String backupFilePath = Paths.get(backupDirectory, "/full_backup_" + databaseDetails.getDbName() + "_" + LocalDateTime.now()).toString();

            String command = String.format("mongodump --host %s --port %d --username %s --password %s --db %s --out %s",
                    databaseDetails.getHost(),
                    databaseDetails.getPort(),
                    databaseDetails.getUsername(),
                    databaseDetails.getPassword(),
                    databaseDetails.getDbName(),
                    backupFilePath);

            executeCommand(command);

            Path path = Paths.get(backupFilePath);

            BackupMetadata metadata = BackupMetadata.builder()
                    .dbType(DatabaseType.MONGODB.toString())
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
    public Path performIncrementalBackup(String backupDirectory, @Nullable String archiveDirectory, @Nullable String walArchivePath) {
        throw new UnsupportedOperationException("Incremental backups are not directly supported in MongoDB.");
    }

    @Override
    public Path performDifferentialBackup(String backupDirectory) {
        throw new UnsupportedOperationException("Differential backup is not supported for MongoDB.");
    }

    @Override
    public void logBackupMetadata(BackupMetadata metadata) {
        logger.info("Backup created: Type={}, Database_name={}, Path={}, Timestamp: {}", metadata.backupType(), metadata.dbName(), metadata.backupFilePath().toString(), metadata.timestamp());

    }
}
