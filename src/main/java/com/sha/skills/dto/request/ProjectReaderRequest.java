package com.sha.skills.dto.request;

import com.sha.skills.enums.ScanProjectOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectReaderRequest {

    private String projectPath;
    private ScanProjectOperation operation;
    private String fileName;
    private String searchText;
}
