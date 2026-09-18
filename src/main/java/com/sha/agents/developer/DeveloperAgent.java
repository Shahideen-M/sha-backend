package com.sha.agents.developer;

import com.sha.agents.developer.dto.DeveloperRequest;
import com.sha.agents.developer.dto.DeveloperResponse;
import com.sha.agents.developer.enums.DeveloperOperation;
import com.sha.agents.developer.service.DeveloperService;
import com.sha.agents.enums.AgentType;
import com.sha.agents.service.Agent;
import com.sha.brain.dto.OperationPrompt;
import com.sha.brain.prompt.AgentPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeveloperAgent implements Agent<DeveloperRequest, DeveloperResponse> {

    private final DeveloperService developerService;

    @Override
    public AgentType getType() {
        return AgentType.DEVELOPER;
    }

    @Override
    public DeveloperResponse executeTyped(DeveloperRequest request) {
        return switch (request.getOperation()) {
            case DEVELOP -> developerService.develop(request);
        };
    }

    @Override
    public Class<DeveloperRequest> getRequestClass() {
        return DeveloperRequest.class;
    }

    @Override
    public DeveloperResponse execute(Object request) {
        return executeTyped((DeveloperRequest) request);
    }

    @Override
    public AgentPrompt<DeveloperOperation> describe() {
        return new AgentPrompt<>(
                AgentType.DEVELOPER,
                "Develop software projects using Sha's controlled development tools.",
                List.of(
                        "develop project",
                        "fix code",
                        "fix bug",
                        "modify project",
                        "debug project",
                        "developer"
                ),
                List.of(
                        new OperationPrompt<>(
                                DeveloperOperation.DEVELOP,
                                "Perform a development task on a software project.",
                                List.of("projectPath", "task"),
                                """
                                {
                                  "projectPath":"D:\\Projects\\Sha",
                                  "task":"Fix the failing authentication test",
                                  "operation":"DEVELOP"
                                }
                                """
                        )
                )
        );
    }
}