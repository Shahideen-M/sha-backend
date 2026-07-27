package com.sha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileResponse {

    private boolean success;
    private String message;

    private String fileName;
    private String extension;
    private String content;

    private List<String> files;

    // read
    public FileResponse(String fileName, String extension, String content) {
        this.success = true;
        this.fileName = fileName;
        this.extension = extension;
        this.content = content;
    }

    // write
    public FileResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    //list
    public FileResponse(List<String> files) {
        this.success = true;
        this.files = files;
    }
}
