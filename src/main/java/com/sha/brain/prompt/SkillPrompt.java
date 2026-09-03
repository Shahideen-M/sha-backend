package com.sha.brain.prompt;

import com.sha.brain.dto.OperationPrompt;
import com.sha.brain.enums.SkillType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillPrompt<T extends Enum<T>> {
    private SkillType skill;
    private String purpose;
    private List<String> keywords;
    private List<OperationPrompt<T>> operations;
}
