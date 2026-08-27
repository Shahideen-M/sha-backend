package com.sha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoEditorResponse {

    private boolean success;
    private String message;
    private String outputPath;
    private Double duration;
    private List<String> operations;

}