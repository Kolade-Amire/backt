package com.kolade.backt.mysql;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service("mysql")
@RequiredArgsConstructor
public class MySQLBackupService implements BackupService {

    private static final Logger logger = LoggerFactory.getLogger(MySQLBackupService.class);
    private final DatabaseDetailsService databaseDetailService;

    private void executeCommand(String command) {

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

                boolean isFinished = process.waitFor(600, TimeUnit.SECONDS);
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

    /**
     * Performs a full backup of the MySQL database to the specified directory.
     *
     * @param backupDirectory The directory path where the backup file will be saved
     * @return Path to the backup file
     */

    @Override
    public Path performFullBackup(String backupDirectory) {
        DatabaseDetails databaseDetails = databaseDetailService.getActiveDatabaseDetails();
        if (databaseDetails == null) {
            throw new CustomBacktException("No active database connection");
        }

        try {
            String backupFilePath = Paths.get(backupDirectory, "/full_backup_" + databaseDetails.getDbName() + "_" + LocalDateTime.now() + ".sql").toString();
            String command = String.format("mysqldump -h %s -P %d -u %s -p%s %s > %s",
                    databaseDetails.getHost(),
                    databaseDetails.getPort(),
                    databaseDetails.getUsername(),
                    databaseDetails.getPassword(),
                    databaseDetails.getDbName(),
                    backupFilePath);

            executeCommand(command);

            Path path = Paths.get(backupFilePath);

            BackupMetadata metadata = BackupMetadata.builder()
                    .dbType(DatabaseType.MYSQL.toString())
                    .backupFilePath(path)
                    .backupType(BackupType.FULL)
                    .dbName(databaseDetails.getDbName())
                    .timestamp(LocalDateTime.now())
                    .build();

            logBackupMetadata(metadata);

            return path;
        } catch (Exception e) {
            throw new CustomBacktException("Failed to perform backup operation: ", e);
        }
    }

    @Override
    public Path performIncrementalBackup(String backupDirectory, @Nullable String archiveDirectory, @Nullable String walArchivePath) {

        DatabaseDetails databaseDetails = databaseDetailService.getActiveDatabaseDetails();
        if (databaseDetails == null) {
            throw new CustomBacktException("No active database connection");
        }

        try {
            String backupFilePath = Paths.get(backupDirectory, "/incremental_backup_" + databaseDetails.getDbName() + "_" + LocalDateTime.now() + ".sql").toString();
            String command = String.format("mysqlbinlog --host=%s --port=%d --start-datetime='YYYY-MM-DD HH:MM:SS' > %s", databaseDetails.getHost(), databaseDetails.getPort(), backupFilePath);
            executeCommand(command);
            Path path = Paths.get(backupFilePath);
            BackupMetadata metadata = BackupMetadata.builder()
                    .backupFilePath(path)
                    .backupType(BackupType.INCREMENTAl)
                    .dbName(databaseDetails.getDbName())
                    .timestamp(LocalDateTime.now())
                    .build();

            logBackupMetadata(metadata);

            return path;
        } catch (Exception e) {
            throw new CustomBacktException("Failed to perform backup operation: ", e);
        }
    }

    @Override
    public Path performDifferentialBackup(String backupDirectory) {
        throw new UnsupportedOperationException("Differential backup is not supported for MySQL.");
    }

    @Override
    public void logBackupMetadata(BackupMetadata metadata) {
        logger.info("Backup completed. Type: {}, Database_name: {}, Path: {}, Timestamp: {}", metadata.backupType(), metadata.dbName(), metadata.backupFilePath(), metadata.timestamp());

        String jsonMetadata = String.format("{\"backupType\": \"%s\", \"dbName\": \"%s\", \"filePath\": \"%s\", \"timestamp\": \"%s\"}\n", metadata.backupType(), metadata.dbName(), metadata.backupFilePath().toString(), metadata.timestamp());

        try {
            Files.writeString(Paths.get("backup_metadata.json"), jsonMetadata, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            logger.error("Failed to write backup metadata", e);
        }
    }

    //To extract database name from connection url
//    private String extractDatabaseName(String url) {
//        String regex = "//[^/]+/([a-zA-Z0-9_]+)";
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(url);
//
//        if (matcher.find()) {
//            return matcher.group(1);
//        } else {
//            throw new IllegalArgumentException("Invalid URL format: " + url);
//        }
//    }

}
