package com.sha.service;

import com.sha.brain.prompt.SkillPrompt;
import com.sha.enums.SkillType;

public interface Skill<REQ, RES> {

    SkillType getType();
    RES executeTyped(REQ request);
    Class<REQ> getRequestClass();
    RES execute(Object request);
    SkillPrompt<?> describe();
}
