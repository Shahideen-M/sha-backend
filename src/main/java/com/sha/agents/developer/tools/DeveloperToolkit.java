package com.sha.agents.developer.tools;

import com.sha.agents.tools.AgentShaTool;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class DeveloperToolkit {

    private static final int MAX_PROJECTS = 16;

    private final ConcurrentMap<Path, Entry> entries = new ConcurrentHashMap<>();

    private record Entry(ProjectBoundary boundary, List<AgentShaTool> tools) {
    }

    public List<AgentShaTool> forProject(String projectPath) {
        ProjectBoundary boundary = ProjectBoundary.forProject(projectPath);
        if (entries.size() >= MAX_PROJECTS
                && !entries.containsKey(boundary.root())) {
            entries.clear();
        }
        Entry entry = entries.computeIfAbsent(
                boundary.root(),
                root -> new Entry(boundary, DeveloperTools.forProject(boundary))
        );
        return entry.tools();
    }

    public ProjectBoundary boundary(String projectPath) {
        return ProjectBoundary.forProject(projectPath);
    }
}