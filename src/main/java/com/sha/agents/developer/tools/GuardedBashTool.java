package com.sha.agents.developer.tools;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuardedBashTool implements ToolCallback {

    private static final Pattern COMMAND_PATTERN = Pattern.compile(
            "\"command\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
    );

    private final ToolCallback delegate;
    private final ProjectBoundary boundary;

    GuardedBashTool(ToolCallback delegate, ProjectBoundary boundary) {
        this.delegate = delegate;
        this.boundary = boundary;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
    }

    @Override
    public String call(String toolInput) {
        return call(toolInput, null);
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        String command = extractCommand(toolInput);
        if (command == null) {
            return "Error: cannot parse command argument for Bash.";
        }
        String violation = boundary.validateCommand(command);
        if (violation != null) {
            return "Error: command refused by project boundary: " + violation;
        }
        return delegate.call(toolInput, toolContext);
    }

    private static String extractCommand(String toolInput) {
        if (!StringUtils.hasText(toolInput)) {
            return null;
        }
        Matcher matcher = COMMAND_PATTERN.matcher(toolInput);
        if (matcher.find()) {
            String escaped = matcher.group(1);
            return escaped.replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return null;
    }
}