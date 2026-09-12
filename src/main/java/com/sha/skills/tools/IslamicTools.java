package com.sha.skills.tools;

import com.sha.skills.IslamicSkill;
import com.sha.skills.Skill;
import com.sha.skills.dto.request.IslamicRequest;
import com.sha.skills.dto.response.IslamicResponse;
import com.sha.skills.enums.IslamicOperation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Getter
@Component
@RequiredArgsConstructor
public class IslamicTools implements ShaTool {

    private final IslamicSkill islamicSkill;
    private final ObjectMapper objectMapper;

    @Override
    public Skill<?, ?> getSkill() {
        return islamicSkill;
    }

    @Override
    public Object createRequest(String toolName, String arguments) {

        IslamicRequest request = new IslamicRequest();
        var json = objectMapper.readTree(arguments);

        switch (toolName) {
            case "playSurah" -> {
                request.setSurah(json.get("surahName").asString());
                request.setOperation(IslamicOperation.PLAY_SURAH);
            }
            default -> throw new IllegalArgumentException("Unknown IslamicTools operation: " + toolName);
        }
        return request;
    }

    @Tool(description = "playing surah")
    public IslamicResponse playSurah(String surahName) {
        IslamicRequest request = new IslamicRequest();
        request.setSurah(surahName);
        request.setOperation(IslamicOperation.PLAY_SURAH);

        return islamicSkill.execute(request);
    }
}
