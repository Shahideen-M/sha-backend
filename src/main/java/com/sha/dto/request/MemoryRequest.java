package com.sha.dto.request;

import com.sha.enums.MemoryOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemoryRequest {

    private Long id;
    private String content;
    private String category;
    private String query;
    private MemoryOperation operation;
}