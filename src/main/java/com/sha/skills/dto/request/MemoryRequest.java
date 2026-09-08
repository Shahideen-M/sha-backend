package com.sha.skills.dto.request;

import com.sha.skills.enums.MemoryOperation;
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