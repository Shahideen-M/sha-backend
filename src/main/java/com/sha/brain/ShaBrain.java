package com.sha.brain;

import com.sha.brain.dto.ExecutionPlan;
import com.sha.brain.dto.ExecutionResult;
import com.sha.brain.dto.ShaBrainResponse;
import com.sha.brain.enums.ShaResponseType;
import com.sha.skills.enums.SkillType;
import com.sha.skills.tools.FileTools;
import com.sha.skills.tools.IslamicTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class ShaBrain {

    private final ChatClient chatClient;
    private final FileTools fileTools;
    private final IslamicTools islamicTools;

    public ShaBrain(@Qualifier("geminiChatClient") ChatClient chatClient,
                    FileTools fileTools, IslamicTools islamicTools) {
        this.chatClient = chatClient;
        this.fileTools = fileTools;
        this.islamicTools = islamicTools;
    }

    public ShaBrainResponse process(String userMessage) {

        String response = chatClient.prompt()
                .system("You are Sha, a helpful AI assistant.")
                .user(userMessage)
                .advisors(advisor -> {
                    advisor.param(ChatMemory.CONVERSATION_ID, "default");
                })
                .tools(fileTools, islamicTools)
                .call()
                .content();

        System.out.println(response);
        return new ShaBrainResponse(
                ShaResponseType.EXECUTION_RESULT,
                response,
                null,
                false,
                ""
        );
    }
}