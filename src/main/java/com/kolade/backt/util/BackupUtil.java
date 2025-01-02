package com.kolade.backt.util;

import com.kolade.backt.common.BackupRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

public class BackupUtil {

    public static String generateBackupId(BackupRequest request, LocalDateTime startTime) {
        String uniqueId = UUID.randomUUID().toString();
        String cleanDbName = "";
        if(request.databaseName() == null || request.databaseName().isBlank()){
            cleanDbName =  "UNKNOWN";
        }else{
            //remove special characters
            cleanDbName = request.databaseName().replaceAll("[^a-zA-Z0-9_-]", "").toUpperCase();
        }
        //capture metadata and add to the uniqueId
        return String.format("BACKUP-%s-%s-%s-%s", request.backupType(), cleanDbName, startTime, uniqueId);
    }

    public static Path createTempBackupPath(String backupId) throws IOException {
        //base directory for backups
        Path tempDir = Files.createTempDirectory("backup");

        //combine base directory with ID to form path
        Path backupPath = tempDir.resolve(backupId);

        //create backup directory
        Files.createDirectories(backupPath);

        return backupPath;
    }

    public static void deleteTempBackupPath(Path filePath) throws IOException {
        Files.deleteIfExists(filePath);
    }
}
