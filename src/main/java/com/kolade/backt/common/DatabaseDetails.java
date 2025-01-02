package com.kolade.backt.common;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@ToString
@Builder
public class DatabaseDetails {
    private String connectionUrl;
    private String username;
    private String password;
    private String host;
    private int port;
    private String databaseName;
}
