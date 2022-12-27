package com.midServer.SetMid.Model;

import lombok.*;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class InstanceModel {
    String instanceName;
    String instanceUrl;
    String instanceUsername;
    String password;
    String midserverName;
    String midserverPassword;
    String Version;

}
