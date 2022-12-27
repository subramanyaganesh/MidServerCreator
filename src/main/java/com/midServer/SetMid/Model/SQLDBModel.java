package com.midServer.SetMid.Model;

import lombok.*;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SQLDBModel {
    String DBName;
    String databaseUsername;
    String databasePassword;
    String DBDumpPath;
}
