package com.midServer.SetMid.Model;

import lombok.*;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ZipModel {
    String zipPath;
    String extractedFilePath;
    String port;

}
