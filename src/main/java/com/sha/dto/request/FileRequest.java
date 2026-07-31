package com.sha.dto.request;

import com.sha.enums.FileOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileRequest {

    private String path;
    private String content = "";
    private FileOperation operation;
    private String searchKeyword;
    private String sourcePath;
    private String destinationPath;

}
