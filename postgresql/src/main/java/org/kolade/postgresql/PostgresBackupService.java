package org.kolade.postgresql;

import lombok.RequiredArgsConstructor;
import org.kolade.core.*;
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
     * Logs the output and errors of the command execution.
     *
     * @param command    The command to execute, formatted for PostgreSQL tools.
     * @param dbPassword The password for the database.
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
     * Performs a full backup of the PostgreSQL database.
     * Generates a backup file in the specified directory and logs metadata.
     *
     * @param backupDirectory The directory path where the backup file will be saved.
     * @return The path to the generated backup file.
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
                    .dbType(DatabaseType.POSTGRES.toString())
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

    /**
     * Performs an incremental backup using Base Backups and Write-Ahead Logs (WAL).
     * Archives WAL files and restores the database for verification.
     *
     * @param backupDirectory         The directory path where the base backup will be saved.
     * @param archiveDirectory        The directory path where WAL files will be archived.
     * @param postgresWalArchivePath  The source path of PostgreSQL WAL files.
     * @return The path to the base backup file.
     */
    //TODO: give a structure to the file storage behaviour
    @Override
    public Path performIncrementalBackup(String backupDirectory, String archiveDirectory, String postgresWalArchivePath) {
        Path baseBackupPath = performBaseBackup(backupDirectory);
        archiveWALFiles(archiveDirectory, postgresWalArchivePath);

        BackupMetadata metadata = BackupMetadata.builder()
                .dbType(DatabaseType.POSTGRES.toString())
                .backupFilePath(baseBackupPath)
                .backupType(BackupType.INCREMENTAl)
                .dbName(databaseDetailsService.getActiveDatabaseDetails().getDbName())
                .timestamp(LocalDateTime.now())
                .build();

        restoreDatabase(backupDirectory, archiveDirectory);

        logBackupMetadata(metadata);

        return baseBackupPath;

    }

    /**
     * Performs a base backup of the PostgreSQL database.
     *
     * @param backupDirectory The directory path where the base backup will be saved.
     * @return The path to the base backup file.
     */
    private Path performBaseBackup(String backupDirectory) {
        DatabaseDetails databaseDetails = databaseDetailsService.getActiveDatabaseDetails();
        if (databaseDetails == null) {
            throw new CustomBacktException("No active database connection");
        }

        try {
            String backupFilePath = Paths.get(backupDirectory, "base_backup_" + LocalDateTime.now() + ".tar").toString();
            String command = String.format("pg_basebackup -h %s -p %d -U %s -D %s -Ft -z -P",
                    databaseDetails.getHost(),
                    databaseDetails.getPort(),
                    databaseDetails.getUsername(),
                    backupFilePath);

            executeCommand(command, databaseDetails.getPassword());

            return Paths.get(backupFilePath);
        } catch (Exception e) {
            throw new CustomBacktException("Failed to perform base backup: ", e);
        }
    }

    /**
     * Archives Write-Ahead Log (WAL) files to a specified directory.
     *
     * @param archiveDirectory       The directory where WAL files will be archived.
     * @param postgresWalArchivePath The source directory of WAL files.
     */
    private void archiveWALFiles(String archiveDirectory, String postgresWalArchivePath) {
        DatabaseDetails databaseDetails = databaseDetailsService.getActiveDatabaseDetails();
        if (databaseDetails == null) {
            throw new CustomBacktException("No active database connection");
        }

        try {
            String archiveCommand = String.format("cp %s/* %s", postgresWalArchivePath, archiveDirectory);
            executeCommand(archiveCommand, databaseDetails.getPassword());
            logger.info("Archived WAL files to {}", archiveDirectory);
        } catch (Exception e) {
            throw new CustomBacktException("Failed to archive WAL files: ", e);
        }
    }


    /**
     * Restores the database from a base backup and replays WAL files.
     *
     * @param baseBackupPath The path to the base backup file.
     * @param walDirectory   The directory containing the WAL files.
     */
    public void restoreDatabase(String baseBackupPath, String walDirectory) {
        DatabaseDetails databaseDetails = databaseDetailsService.getActiveDatabaseDetails();
        if (databaseDetails == null) {
            throw new CustomBacktException("No active database connection");
        }

        try {
            // Restore base backup
            String restoreCommand = String.format("pg_restore -h %s -p %d -U %s -d %s -c %s",
                    databaseDetails.getHost(),
                    databaseDetails.getPort(),
                    databaseDetails.getUsername(),
                    databaseDetails.getDbName(),
                    baseBackupPath);

            executeCommand(restoreCommand, databaseDetails.getPassword());

            // Apply WAL files
            String walReplayCommand = String.format("pg_waldump %s | pg_rewind -h %s -p %d -U %s -D %s",
                    walDirectory,
                    databaseDetails.getHost(),
                    databaseDetails.getPort(),
                    databaseDetails.getUsername(),
                    databaseDetails.getDbName());

            executeCommand(walReplayCommand, databaseDetails.getPassword());
            logger.info("Database restored using base backup and WAL files from {}", walDirectory);

        } catch (Exception e) {
            throw new CustomBacktException("Failed to restore database: ", e);
        }
    }




    @Override
    public Path performDifferentialBackup(String backupDirectory) {
        throw new UnsupportedOperationException("Differential backup is not supported natively for Postgres.");
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
}
