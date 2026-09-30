package com.sha.agents.developer.tools;

import com.sha.agents.tools.AgentShaTool;
import com.sha.brain.enums.AuthorityLevel;
import org.springaicommunity.agent.tools.FileSystemTools;
import org.springaicommunity.agent.tools.GlobTool;
import org.springaicommunity.agent.tools.GrepTool;
import org.springaicommunity.agent.tools.ShellTools;
import org.springaicommunity.agent.tools.TodoWriteTool;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeveloperTools {

    public static List<AgentShaTool> forProject(ProjectBoundary boundary) {
        FileSystemTools fileSystemTools = FileSystemTools
                .builder()
                .allowedDirectories(boundary.root())
                .build();
        GrepTool grepTool = GrepTool
                .builder()
                .allowedDirectories(boundary.root())
                .build();
        GlobTool globTool = GlobTool
                .builder()
                .allowedDirectories(boundary.root())
                .build();
        ShellTools shellTools = ShellTools
                .builder()
                .workingDirectory(boundary.root())
                .build();
        TodoWriteTool todoWriteTool = TodoWriteTool.builder().build();

        List<ToolCallback> callbacks = new ArrayList<>();

        callbacks.addAll(Arrays.asList(ToolCallbacks.from(fileSystemTools)));
        callbacks.addAll(Arrays.asList(ToolCallbacks.from(grepTool)));
        callbacks.addAll(Arrays.asList(ToolCallbacks.from(globTool)));
        callbacks.addAll(Arrays.asList(ToolCallbacks.from(shellTools)));
        callbacks.add(flatTodoWriteCallback(todoWriteTool));

        return callbacks.stream()
                .map(tool -> DeveloperTools.wrap(tool, boundary))
                .toList();
    }

    private static ToolCallback flatTodoWriteCallback(TodoWriteTool delegate) {

        String description = ToolCallbacks.from(delegate)[0]
                .getToolDefinition()
                .description();

        return FunctionToolCallback
                .builder("TodoWrite", (TodoWriteTool.Todos todos) -> delegate.todoWrite(todos))
                .description(description)
                .inputType(TodoWriteTool.Todos.class)
                .build();
    }

    private static AgentShaTool wrap(ToolCallback tool, ProjectBoundary boundary) {

        ToolCallback guarded = "Bash".equals(tool.getToolDefinition().name())
                ? new GuardedBashTool(tool, boundary)
                : tool;

        AuthorityLevel authority = switch (tool.getToolDefinition().name()) {
            case "Read", "Grep", "Glob", "BashOutput", "TodoWrite" -> AuthorityLevel.SAFE;
            case "Write", "Edit", "Bash", "KillShell" -> AuthorityLevel.APPROVAL_REQUIRED;
            default -> AuthorityLevel.APPROVAL_REQUIRED;
        };

        return new AgentShaTool() {

            @Override
            public ToolCallback getTool() {
                return guarded;
            }

            @Override
            public AuthorityLevel getAuthority() {
                return authority;
            }
        };
    }
}