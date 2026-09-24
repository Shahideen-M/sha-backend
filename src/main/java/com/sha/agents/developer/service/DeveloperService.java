package com.sha.agents.developer.service;

import com.sha.agents.developer.dto.DeveloperRequest;
import com.sha.agents.developer.dto.DeveloperResponse;
import com.sha.brain.AgentExecutionService;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.agent.common.workspace.Workspace;
import org.springaicommunity.agent.utils.AgentEnvironment;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class DeveloperService {

    private final AgentExecutionService agentExecutionService;

    public DeveloperResponse develop(DeveloperRequest request) {

        Workspace workspace = Workspace.local(Path.of(request.getProjectPath()));
        String context = """
                Project context:
                %s
                %s
                """
                .formatted(
                        AgentEnvironment.info(workspace),
                        AgentEnvironment.gitStatus(workspace)
                );

        String systemPrompt = """
                You are Sha's Developer Agent.

                Project path:
                %s

                Development task:
                %s

                %s

                You are responsible for completing the development task.

                Planning:
                - For tasks with 3 or more distinct steps, create a
                TodoWrite plan before modifying any files.
                - Break the task into meaningful, actionable steps.
                - Keep exactly one plan item in_progress at a time.
                - Mark items completed immediately when finished.
                - Update the plan as the task progresses.
                - Continue updating the plan after an approval pause
                and resume.
                - Do not claim completion while any required plan
                items remain unfinished.

                First inspect the project and understand the relevant code.

                Use the available development tools to:
                - find files
                - read files
                - search code
                - edit existing files
                - create files when necessary
                - run build and test commands

                Available tools: Read, Write, Edit, Grep, Glob, Bash,
                BashOutput, KillShell, TodoWrite.

                Work iteratively.

                If a build or test fails:
                1. inspect the failure
                2. identify the cause
                3. make the necessary change
                4. run the relevant build or test again

                Do not stop merely because the first build or test fails.

                Work only inside the provided project path. You must
                never read, write, or execute anything outside that path.
                An approval to run a tool does not lift this boundary.
                Do not use paths that escape the project (such as '..',
                an absolute path outside the project, or your home
                directory).

                Never claim that an action succeeded unless the corresponding
                tool actually succeeded.
                """
                .formatted(
                        request.getProjectPath(),
                        request.getTask(),
                        context
                );


        var response = agentExecutionService.execute(
                "developer",
                request.getProjectPath(),
                systemPrompt,
                request.getTask()
        );

        return new DeveloperResponse(
                response.getType() ==
                        com.sha.brain.enums.ShaResponseType.CHAT,
                response.getMessage(),
                response.getType() ==
                        com.sha.brain.enums.ShaResponseType.APPROVAL_REQUIRED,
                response.getApprovalToken()
        );
    }
}