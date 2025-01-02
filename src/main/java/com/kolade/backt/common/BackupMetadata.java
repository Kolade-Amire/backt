package com.kolade.backt.common;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BackupMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String backupId;
    DatabaseType databaseType;
    String backupPath;
    BackupType backupType;
    String databaseName;
    LocalDateTime creationTime;
}
