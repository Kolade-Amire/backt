package org.kolade.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class DatabaseDetails {
    String connectionUrl;
    String username;
    String password;
}
