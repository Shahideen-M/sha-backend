package com.sha.agents.developer.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class ProjectBoundary {

    private final Path root;

    private ProjectBoundary(Path root) {
        this.root = root;
    }

    public static ProjectBoundary forProject(String projectPath) {
        if (projectPath == null || projectPath.isBlank()) {
            throw new IllegalArgumentException("projectPath must not be empty");
        }

        Path path;
        try {
            path = Path.of(projectPath).toAbsolutePath().normalize();
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid projectPath: " + projectPath, e);
        }
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Project path is not an existing directory: " + projectPath);
        }
        try {
            path = path.toRealPath();
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot resolve project path: " + projectPath, e);
        }
        return new ProjectBoundary(path);
    }

    public Path root() {
        return root;
    }

    public boolean isWithin(Path path) {
        Path absolute;
        try {
            absolute = path.toAbsolutePath().normalize();
        } catch (RuntimeException e) {
            return false;
        }
        for (Path component : absolute) {
            if ("..".equals(component.toString())) {
                return false;
            }
        }
        return absolute.startsWith(root);
    }

    public String validateCommand(String command) {
        if (command == null || command.isBlank()) return null;

        String[] tokens = command.split("\\s+");
        for (int i = 0; i < tokens.length; i++) {
            String token = stripQuotes(tokens[i]);
            if (token.isEmpty()) {
                continue;
            }
            if (isDirectorySwitch(token)) {
                int targetIndex = i + 1;
                if ((token.equalsIgnoreCase("cd")
                        || token.equalsIgnoreCase("pushd"))
                        && targetIndex < tokens.length
                        && tokens[targetIndex].equalsIgnoreCase("/d")) {
                    targetIndex++;
                }
                if (targetIndex >= tokens.length) {
                    return token + " requires a target directory";
                }
                String violation = inspect(stripQuotes(tokens[targetIndex]));
                if (violation != null) {
                    return token + " target: " + violation;
                }
                i = targetIndex;
                continue;
            }
            String violation = inspect(token);
            if (violation != null) {
                return violation;
            }
        }
        return null;
    }

    private String inspect(String token) {
        if (isTraversal(token)) {
            return "path traversal ('..')";
        }
        if (isUserHomeReference(token)) {
            return "references the user home directory";
        }
        if (token.startsWith("\\\\")) {
            return "references a network share";
        }
        if (token.matches("(?i)^[a-z]:[\\\\/].*")) {
            return isWithin(Path.of(token))
                    ? null
                    : "uses an absolute path outside the project";
        }
        if (token.startsWith("/")) {
            return isWithin(Path.of(token))
                    ? null
                    : "uses an absolute path outside the project";
        }
        return null;
    }

    private boolean isDirectorySwitch(String token) {
        return token.equalsIgnoreCase("cd")
                || token.equalsIgnoreCase("pushd")
                || token.equals("-C")
                || token.equals("--git-dir")
                || token.equals("--work-tree")
                || token.equals("--directory");
    }

    private boolean isTraversal(String token) {
        return token.equals("..")
                || token.startsWith("../")
                || token.startsWith("..\\")
                || token.endsWith("/..")
                || token.endsWith("\\..")
                || token.contains("/../")
                || token.contains("\\..\\");
    }

    private boolean isUserHomeReference(String token) {
        String lower = token.toLowerCase(Locale.ROOT);
        return token.startsWith("~")
                || lower.contains("$home")
                || lower.contains("%userprofile%")
                || lower.contains("%homedrive%")
                || lower.contains("%homepath%");
    }

    private static String stripQuotes(String token) {
        if (token.length() >= 2
                && ((token.startsWith("\"") && token.endsWith("\""))
                || (token.startsWith("'") && token.endsWith("'")))) {
            return token.substring(1, token.length() - 1);
        }
        return token;
    }
}