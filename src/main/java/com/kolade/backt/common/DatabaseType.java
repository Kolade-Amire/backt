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

    public static DatabaseType getTypeByDisplayName(String name) {
        return Arrays.stream(DatabaseType.values())
                .filter(type -> type.getDisplayName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported database type: " + name));
    }

}
