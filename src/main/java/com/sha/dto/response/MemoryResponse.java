package com.sha.dto.response;

import com.sha.entity.Memory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemoryResponse {

    private boolean success;
    private String message;
    private Memory memory;
    private List<Memory> memories;
}