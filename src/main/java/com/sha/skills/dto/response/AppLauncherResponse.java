package com.sha.skills.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppLauncherResponse {

    private boolean success;
    private String message;
}
