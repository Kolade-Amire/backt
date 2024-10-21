package org.kolade.core;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@ToString
@Builder
public class DatabaseDetails {
    String connectionUrl;
    String username;
    String password;
}
