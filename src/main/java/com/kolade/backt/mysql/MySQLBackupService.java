package com.kolade.backt.mysql;

import com.kolade.backt.common.*;
import com.kolade.backt.exception.BackupException;
import com.kolade.backt.exception.CustomBacktException;
import com.kolade.backt.repository.BackupMetadataRepository;
import com.kolade.backt.service.BackupService;
import com.kolade.backt.service.DatabaseDetailsService;
import com.kolade.backt.util.BackupUtil;
import jakarta.persistence.EntityNotFoundException;
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
    private final BackupMetadataRepository metadataRepository;

    private void executeCommand(String command) {

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
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

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    process.destroyForcibly();
                    throw new BackupException("mysqldump failed with exit code: " + exitCode);
                }

            }

        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new CustomBacktException("An error occurred while executing command... \n " + e.getMessage());
        }
    }

    @Override
    public BackupResult performBackup(BackupRequest backupRequest) throws IOException {
        validateBackupRequest(backupRequest);
        var startTime = LocalDateTime.now();
        var id = BackupUtil.generateBackupId(backupRequest, startTime);
        Path tempBackupPath = BackupUtil.createTempBackupPath(id);

        try {
            switch (backupRequest.backupType()) {
                case FULL -> performFullBackup(backupRequest, tempBackupPath);
                case INCREMENTAl -> performIncrementalBackup(backupRequest, tempBackupPath);
                case DIFFERENTIAL -> performDifferentialBackup(backupRequest, tempBackupPath);
            }

            //compress if requested
//            Path finalBackupPath = backupRequest.compress()
//                    ? //call compression service
//                    : moveToFinalLocation(tempBackupPath, backupRequest.destinationPath());
            //save metadata

            var backupMetadata = BackupMetadata.builder()
                    .backupId(id)
                    .databaseType(DatabaseType.MYSQL)
                    .backupType(backupRequest.backupType())
                    .databaseName(backupRequest.databaseName())
                    .backupPath(backupRequest.destinationPath().toString()) //edit to finalPath
                    .creationTime(startTime)
                    .build();
            metadataRepository.save(backupMetadata);

            return BackupResult.builder()
                    .backupId(id)
                    .startTime(startTime)
                    .backupType(backupRequest.backupType())
                    .endTime(LocalDateTime.now())
                    .backupFilePath(backupRequest.destinationPath()) //edit to finalPath
                    .backupStatus(BackupStatus.SUCCESS)
                    .sizeInBytes(Files.size(backupRequest.destinationPath())) //edit to finalPath
                    .build();

        } catch (Exception e) {
            logger.error("Backup failed", e);
            return BackupResult.builder()
                    .backupId(id)
                    .startTime(startTime)
                    .backupType(backupRequest.backupType())
                    .endTime(LocalDateTime.now())
                    .backupFilePath(backupRequest.destinationPath()) //edit to finalPath
                    .backupStatus(BackupStatus.FAILED)
                    .sizeInBytes(Files.size(backupRequest.destinationPath())) //edit to finalPath
                    .errorMessage(e.getMessage())
                    .build();
        } finally {
            BackupUtil.deleteTempBackupPath(tempBackupPath);
        }


    }

    @Override
    public void validateBackupRequest(BackupRequest request) {
        if (request.databaseName() == null || request.databaseName().isBlank()) {
            throw new IllegalArgumentException("Database name is required");
        }
        if (request.backupType() == null) {
            throw new IllegalArgumentException("Backup type is required");
        }
        if (request.destinationPath() == null) {
            throw new IllegalArgumentException("Destination path is required");
        }
    }

    @Override
    public BackupMetadata getBackupMetadata(String backupId) {
        return metadataRepository.findByBackupId(backupId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Backup %s not found", backupId))
        );
    }


    private void performFullBackup(BackupRequest request, Path tempBackupPath) {
        DatabaseDetails databaseDetails = databaseDetailService.getActiveDatabaseDetails();
        if (databaseDetails == null) {
            throw new CustomBacktException("No active database connection");
        }

        try {

            String command = "mysqldump" +
                    " --host=" + databaseDetails.getHost() +
                    " --port=" + databaseDetails.getPort() +
                    " --user=" + databaseDetails.getUsername() +
                    " --password=\"" + databaseDetails.getPassword() + "\"" +
                    " --result-file=" + tempBackupPath +
                    " --databases \"" +
                    request.databaseName() + "\"";

            executeCommand(command);

        } catch (Exception e) {
            throw new BackupException("Failed to perform backup operation: ", e);
        }
    }


    public Path performIncrementalBackup(String backupDirectory, @Nullable String archiveDirectory, @Nullable String walArchivePath) {

        DatabaseDetails databaseDetails = databaseDetailService.getActiveDatabaseDetails();
        if (databaseDetails == null) {
            throw new CustomBacktException("No active database connection");
        }

        try {
            String backupFilePath = Paths.get(backupDirectory, "/incremental_backup_" + databaseDetails.getDatabaseName() + "_" + LocalDateTime.now() + ".sql").toString();
            String command = String.format("mysqlbinlog --host=%s --port=%d --start-datetime='YYYY-MM-DD HH:MM:SS' > %s", databaseDetails.getHost(), databaseDetails.getPort(), backupFilePath);
            executeCommand(command);
            Path path = Paths.get(backupFilePath);
            BackupMetadataDto metadata = BackupMetadataDto.builder()
                    .backupFilePath(path)
                    .backupType(BackupType.INCREMENTAl)
                    .dbName(databaseDetails.getDatabaseName())
                    .timestamp(LocalDateTime.now())
                    .build();

            getBackupMetadata(metadata);

            return path;
        } catch (Exception e) {
            throw new CustomBacktException("Failed to perform backup operation: ", e);
        }
    }

    public Path performDifferentialBackup(String backupDirectory) {
        // For PostgreSQL, differential backup is similar to incremental
        // as it relies on WAL archiving
        return performIncrementalBackup(backupDirectory, null, null);
    }

}
