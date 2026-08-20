package com.sha.brain;

import com.sha.brain.dto.ShaBrainResponse;
import com.sha.brain.enums.ShaResponseType;
import org.springframework.stereotype.Service;

@Service
public class ShaBrainValidator {

    public void validate(ShaBrainResponse response) {
        if (response.getType() == ShaResponseType.CHAT) validateChat(response);
        if (response.getType() == ShaResponseType.SKILL_CALL) validateSkillCall(response);
    }

    public void validateChat(ShaBrainResponse response) {
        if (response.getMessage() != null) return;
        else throw new RuntimeException("Message is required for CHAT response.");
    }

    public void validateSkillCall(ShaBrainResponse response) {
        if (response.getSkill() == null) throw new RuntimeException("Skill is required.");
        if (response.getOperation() == null) throw new RuntimeException("Operation is required.");
        if (isParameterlessOperation(response.getOperation())) return;
        if (response.getParameters() == null) throw new RuntimeException("Parameters are required.");
    }

    private boolean isParameterlessOperation(String operation) {
        return switch (operation) {
            case "SURAH_LIST",
                 "READ_PAGE",
                 "CLOSE_BROWSER" -> true;
            default -> false;
        };
    }
}
