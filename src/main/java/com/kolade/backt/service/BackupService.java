package com.kolade.backt.service;

import com.kolade.backt.common.BackupMetadata;
import com.kolade.backt.common.BackupMetadataDto;
import com.kolade.backt.common.BackupRequest;
import com.kolade.backt.common.BackupResult;

import java.io.IOException;

public interface BackupService {

    BackupResult performBackup(BackupRequest backupRequest) throws IOException;

    void validateBackupRequest(BackupRequest request);

    BackupMetadata getBackupMetadata(String backupId);

}
