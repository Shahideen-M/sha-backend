package com.sha.dto.request;

import com.sha.enums.DeveloperTaskType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeveloperAssistantRequest {

    private DeveloperTaskType taskType;
    private String content;
}
