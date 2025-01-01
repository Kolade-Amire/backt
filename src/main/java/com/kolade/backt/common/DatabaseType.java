package com.kolade.backt.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum DatabaseType {
    MYSQL("mysql"),
    POSTGRES("postgres"),
    MONGODB("mongodb"),
    SQLITE("sqlite");

    private final String displayName;

    public static boolean isTypeValid(String databaseType) {
        return Arrays.stream(DatabaseType.values())
                .anyMatch(type -> type.getDisplayName().equalsIgnoreCase(databaseType)
                );
    }

}
