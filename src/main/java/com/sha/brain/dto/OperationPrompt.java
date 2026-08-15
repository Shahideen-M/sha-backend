package com.sha.brain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationPrompt<T extends Enum<T>>{
    private T operation;
    private String description;
    private List<String> parameters;
    private String exampleJson;
}
