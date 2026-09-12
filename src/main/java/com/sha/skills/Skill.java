package com.sha.skills;

import com.sha.brain.enums.AuthorityLevel;
import com.sha.brain.prompt.SkillPrompt;
import com.sha.skills.enums.SkillType;

public interface Skill<REQ, RES> {

    SkillType getType();
    RES executeTyped(REQ request);
    Class<REQ> getRequestClass();
    RES execute(Object request);
    SkillPrompt<?> describe();
    AuthorityLevel getAuthority(Object request);
}
