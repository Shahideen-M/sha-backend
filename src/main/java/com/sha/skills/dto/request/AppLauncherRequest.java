package com.sha.skills.dto.request;

import com.sha.skills.enums.LaunchOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppLauncherRequest {

    private String applicationName;
    private String path;
    private LaunchOperation operation;
}
