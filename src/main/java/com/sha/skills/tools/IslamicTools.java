package com.sha.skills.tools;

import com.sha.skills.IslamicSkill;
import com.sha.skills.dto.request.IslamicRequest;
import com.sha.skills.dto.response.IslamicResponse;
import com.sha.skills.enums.IslamicOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IslamicTools {

    private final IslamicSkill islamicSkill;

    @Tool(description = "playing surah")
    public IslamicResponse playSurah(String surahName) {
        IslamicRequest request = new IslamicRequest();
        request.setSurah(surahName);
        request.setOperation(IslamicOperation.PLAY_SURAH);

        return islamicSkill.executeTyped(request);
    }
}
