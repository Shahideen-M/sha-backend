package com.sha.skills.tools;

import com.sha.skills.Skill;

public interface ShaTool {

    Skill<?, ?> getSkill();
    Object createRequest(String toolName, String arguments);
}
