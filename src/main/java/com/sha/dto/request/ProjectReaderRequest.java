package com.sha.dto.request;

import com.sha.enums.ScanProjectOperation;
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
