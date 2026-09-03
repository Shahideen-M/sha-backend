package com.sha.brain.prompt;

import com.sha.agentsData.enums.AgentType;
import com.sha.brain.dto.OperationPrompt;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentPrompt<T extends Enum<T>> {

    private AgentType agent;
    private String purpose;
    private List<String> keywords;
    private List<OperationPrompt<T>> operations;
}