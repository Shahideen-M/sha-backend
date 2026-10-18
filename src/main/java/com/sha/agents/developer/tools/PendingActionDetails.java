package com.sha.agents.developer.tools;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class PendingActionDetails {
    private static final int PREVIEW_LIMIT = 200;

    private PendingActionDetails() {
    }

    public static JsonNode build(
            ObjectMapper objectMapper,
            String toolName,
            String toolArguments,
            String projectPath
    ) {
        JsonNode args = parse(objectMapper, toolArguments);
        String tool = toolName == null ? "" : toolName;

        Map<String, Object> action = switch (tool) {
            case "Write" -> write(args, projectPath);
            case "Edit" -> edit(args);
            case "Bash" -> bash(args);
            case "KillShell" -> killShell(args);
            default -> other(tool, args);
        };

        return objectMapper.valueToTree(action);
    }

    private static Map<String, Object> write(JsonNode args, String projectPath) {
        Map<String, Object> action = base("Write", "write");

        String path = text(args, "filePath");
        String content = text(args, "content");
        String operation = writeOperation(path, projectPath);

        action.put("path", path == null ? "" : path);
        action.put("operation", operation);
        action.put("summary", switch (operation) {
            case "create" -> "Create file " + fileName(path);
            case "overwrite" -> "Overwrite file " + fileName(path);
            default -> "Write file " + fileName(path);
        });

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("bytes", content == null ? 0 : content.getBytes(StandardCharsets.UTF_8).length);
        details.put("preview", preview(content));
        details.put("truncated", content != null && content.length() > PREVIEW_LIMIT);
        action.put("details", details);

        return action;
    }

    private static Map<String, Object> edit(JsonNode args) {
        Map<String, Object> action = base("Edit", "edit");

        String path = text(args, "filePath");
        action.put("path", path == null ? "" : path);
        action.put("operation", "modify");
        action.put("summary", "Edit file " + fileName(path));

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("replaceAll", bool(args, "replace_all"));
        details.put("oldLength", length(text(args, "old_string")));
        details.put("newLength", length(text(args, "new_string")));
        action.put("details", details);

        return action;
    }

    private static Map<String, Object> bash(JsonNode args) {
        Map<String, Object> action = base("Bash", "bash");

        String command = text(args, "command");
        action.put("summary", "Run command: " + (command == null ? "" : command));

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("command", command == null ? "" : command);
        details.put("runInBackground", bool(args, "runInBackground"));
        if (args != null && args.hasNonNull("timeout")) {
            details.put("timeout", args.get("timeout").asLong());
        }
        String description = text(args, "description");
        if (description != null && !description.isBlank()) {
            details.put("description", description);
        }
        action.put("details", details);

        return action;
    }

    private static Map<String, Object> killShell(JsonNode args) {
        Map<String, Object> action = base("KillShell", "kill_shell");

        String bashId = text(args, "bash_id");
        action.put("summary", "Stop background shell " + (bashId == null ? "" : bashId));

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("bashId", bashId == null ? "" : bashId);
        action.put("details", details);

        return action;
    }

    private static Map<String, Object> other(String tool, JsonNode args) {
        Map<String, Object> action = base(tool, "other");
        action.put("summary", "Run tool " + tool);

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("argumentCount", (args != null && args.isObject()) ? args.size() : 0);
        action.put("details", details);

        return action;
    }

    private static Map<String, Object> base(String tool, String kind) {
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("tool", tool);
        action.put("kind", kind);
        return action;
    }

    private static String writeOperation(String path, String projectPath) {
        if (path == null || path.isBlank()) {
            return "unknown";
        }
        try {
            Path target = Path.of(path);
            if (!within(projectPath, target)) {
                return "unknown";
            }
            return Files.exists(target) ? "overwrite" : "create";
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static boolean within(String projectPath, Path target) {
        if (projectPath == null || projectPath.isBlank()) {
            return false;
        }
        try {
            return ProjectBoundary.forProject(projectPath).isWithin(target);
        } catch (Exception e) {
            return false;
        }
    }

    private static String fileName(String path) {
        if (path == null || path.isBlank()) {
            return "file";
        }
        try {
            Path name = Path.of(path).getFileName();
            return name == null ? path : name.toString();
        } catch (Exception e) {
            return path;
        }
    }

    private static JsonNode parse(ObjectMapper objectMapper, String toolArguments) {
        if (toolArguments == null || toolArguments.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(toolArguments);
        } catch (Exception e) {
            return null;
        }
    }

    private static String text(JsonNode args, String field) {
        if (args == null) {
            return null;
        }
        JsonNode value = args.get(field);
        return (value == null || value.isNull()) ? null : value.asString();
    }

    private static boolean bool(JsonNode args, String field) {
        if (args == null) {
            return false;
        }
        JsonNode value = args.get(field);
        return value != null && value.asBoolean(false);
    }

    private static int length(String value) {
        return value == null ? 0 : value.length();
    }

    private static String preview(String content) {
        if (content == null) {
            return "";
        }
        return content.length() <= PREVIEW_LIMIT ? content : content.substring(0, PREVIEW_LIMIT);
    }
}
