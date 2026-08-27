package com.sha.brain;

import com.sha.brain.enums.AuthorityLevel;
import com.sha.enums.SkillType;
import org.springframework.stereotype.Service;

@Service
public class AuthorityManager {

    public AuthorityLevel check(SkillType skill, String operation) {
        if (skill == SkillType.FILE) {
            return switch (operation) {
                case "READ", "LIST", "SEARCH" -> AuthorityLevel.SAFE;
                case "WRITE", "UPDATE", "DELETE", "COPY", "RENAME" -> AuthorityLevel.APPROVAL_REQUIRED;
                default -> AuthorityLevel.BLOCKED;
            };
        }
        return AuthorityLevel.SAFE;
    }
}
