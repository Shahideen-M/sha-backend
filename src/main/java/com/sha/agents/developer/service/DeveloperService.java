package com.sha.agents.developer.service;

import com.sha.agents.developer.dto.DeveloperRequest;
import com.sha.agents.developer.dto.DeveloperResponse;
import com.sha.brain.AgentExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeveloperService {

    private final AgentExecutionService agentExecutionService;

    public DeveloperResponse develop(DeveloperRequest request) {

        String systemPrompt = """
                You are Sha's Developer Agent.

                Project path:
                %s

                Development task:
                %s

                Work on the project using the available development tools.

                First inspect the project and understand the relevant code.
                Make only the changes necessary for the requested task.
                Build and test the project when appropriate.
                If a build or test fails, inspect the failure and continue fixing it.

                Never claim an action succeeded unless the corresponding
                tool actually succeeded.

                Work only inside the provided project path.
                """
                .formatted(
                        request.getProjectPath(),
                        request.getTask()
                );

        var response = agentExecutionService.execute(
                "developer",
                systemPrompt,
                request.getTask()
        );

        return new DeveloperResponse(
                response.getType() ==
                        com.sha.brain.enums.ShaResponseType.CHAT,
                response.getMessage()
        );
    }
}