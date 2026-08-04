package com.sha.service;

import com.sha.enums.SkillType;

public interface Skill<REQ, RES> {

    SkillType getType();
    RES executeTyped(REQ request);
    Class<REQ> getRequestClass();
    RES execute(Object request);
}
