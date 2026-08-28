package com.sha.brain.dto;

import com.sha.enums.SkillType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StepResult {

    private SkillType skill;
    private String operation;
    private boolean success;
    private String message;
}
