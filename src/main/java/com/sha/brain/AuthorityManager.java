package com.sha.brain;

import com.sha.brain.enums.AuthorityLevel;
import com.sha.brain.enums.ExecutionTargetType;
import com.sha.skills.enums.FileOperation;
import org.springframework.stereotype.Service;

@Service
public class AuthorityManager {

    public AuthorityLevel check(
            ExecutionTargetType type,
            String target,
            FileOperation operation) {
        if (type == ExecutionTargetType.SKILL && "FILE".equals(target)) {
            return switch (operation) {
                case READ, LIST, SEARCH -> AuthorityLevel.SAFE;
                case WRITE, UPDATE, COPY, RENAME -> AuthorityLevel.APPROVAL_REQUIRED;
                case DELETE -> AuthorityLevel.BLOCKED;
            };
        }
        return AuthorityLevel.SAFE;
    }
}
