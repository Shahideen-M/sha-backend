package com.sha.service.skills;

import com.sha.brain.dto.OperationPrompt;
import com.sha.brain.prompt.SkillPrompt;
import com.sha.dto.request.AppLauncherRequest;
import com.sha.dto.response.AppLauncherResponse;
import com.sha.enums.LaunchOperation;
import com.sha.brain.enums.SkillType;
import com.sha.service.Skill;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
public class AppLauncherSkill implements Skill<AppLauncherRequest, AppLauncherResponse> {


    @Override
    public SkillType getType() {
        return SkillType.APP;
    }

    @Override
    public AppLauncherResponse executeTyped(AppLauncherRequest request) {

        return switch (request.getOperation()) {
            case OPEN_APPLICATION -> openApp(request);
            case OPEN_FILE -> openFile(request);
            case OPEN_FOLDER -> openFolder(request);
        };
    }

    @Override
    public Class<AppLauncherRequest> getRequestClass() {
        return AppLauncherRequest.class;
    }

    @Override
    public AppLauncherResponse execute(Object request) {
        return executeTyped((AppLauncherRequest) request);
    }

    @Override
    public SkillPrompt<LaunchOperation> describe() {
        return new SkillPrompt<>(
                SkillType.APP,
                "Open desktop applications, files and folders.",
                List.of(
                        "open",
                        "launch",
                        "start",
                        "application",
                        "app",
                        "program",
                        "file",
                        "folder"
                ),
                List.of(
                        new OperationPrompt<>(
                                LaunchOperation.OPEN_APPLICATION,
                                "Launch an installed desktop application.",
                                List.of("applicationName"),
                                """
                                {
                                  "applicationName":"Chrome",
                                  "operation":"OPEN_APPLICATION"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                LaunchOperation.OPEN_FILE,
                                "Open an existing file.",
                                List.of("path"),
                                """
                                {
                                  "path":"D:/Notes/test.txt",
                                  "operation":"OPEN_FILE"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                LaunchOperation.OPEN_FOLDER,
                                "Open an existing folder.",
                                List.of("path"),
                                """
                                {
                                  "path":"D:/Downloads",
                                  "operation":"OPEN_FOLDER"
                                }
                                """
                        )
                )
        );
    }

    public AppLauncherResponse openApp(AppLauncherRequest request) {

        String command = APPLICATIONS.get(request.getApplicationName().toLowerCase());

        if (command == null) {
            throw new RuntimeException("Application not supported: " + request.getApplicationName());
        }
        try {
            new ProcessBuilder(command).start();
            return new AppLauncherResponse(true, "Opened "+request.getApplicationName());
        } catch (IOException e) {
            throw new RuntimeException("Failed to open application: "+ request.getApplicationName(), e);
        }
    }

    public AppLauncherResponse openFile(AppLauncherRequest request) {

        try {
            Path path = Path.of(request.getPath());
            if (!Files.exists(path)) throw new RuntimeException("File not found: " + path);

            if (!Files.isRegularFile(path)) throw new RuntimeException("Not a file: " + path);

            new ProcessBuilder(
                    "cmd",
                    "/c",
                    "start",
                    "",
                    path.toString()
            ).start();
            return new AppLauncherResponse(true, "Opened file: " + path.getFileName());
        } catch(IOException e) {
            throw new RuntimeException("Cannot open file: "+ request.getPath(), e);
        }
    }

    public AppLauncherResponse openFolder(AppLauncherRequest request) {

        try {
            Path path = Path.of(request.getPath());

            if (!Files.exists(path)) throw new RuntimeException("Folder not found: " +path);
            if (!Files.isDirectory(path)) throw new RuntimeException("Not a directory: " +path);

            new ProcessBuilder(
                    "explorer.exe",
                    path.toString()
            ).start();
            return new AppLauncherResponse(true, "Opened folder: " +path.getFileName());

        } catch (IOException e) {
            throw new RuntimeException("Cannot open folder: " + request.getPath(), e);
        }
    }

    private static final Map<String, String> APPLICATIONS = Map.ofEntries(
            Map.entry("notepad", "notepad.exe"),
            Map.entry("calculator", "calc.exe"),
            Map.entry("paint", "mspaint.exe"),
            Map.entry("explorer", "explorer.exe"),
            Map.entry("vs code", "C:\\Users\\HP\\AppData\\Local\\Programs\\Microsoft VS Code\\bin\\Code.cmd"),
            Map.entry("visual studio code", "C:\\Users\\HP\\AppData\\Local\\Programs\\Microsoft VS Code\\bin\\Code.cmd"),
            Map.entry("intellij", "C:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2025.2.1\\bin\\idea64.exe"),
            Map.entry("cmd", "cmd.exe")
    );
}