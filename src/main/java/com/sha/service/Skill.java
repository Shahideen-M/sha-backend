package com.sha.service;

import com.sha.enums.SkillType;

public interface Skill<REQ, RES> {

    SkillType getType();
    RES execute(REQ request);
}
