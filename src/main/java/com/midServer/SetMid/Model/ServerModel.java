package com.midServer.SetMid.Model;

import lombok.*;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ServerModel {
    String ipAddress;
    String serverUsername;
    String serverPassword;
    String command;
    String pemFilePath;
    String jumpBoxIP;
    String jumpBoxUser;
}
