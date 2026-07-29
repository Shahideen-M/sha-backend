package com.sha.service.skills;

import com.sha.dto.request.AppLauncherRequest;
import com.sha.dto.response.AppLauncherResponse;
import com.sha.enums.SkillType;
import com.sha.service.Skill;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
public class AppLauncherSkill implements Skill<AppLauncherRequest, AppLauncherResponse> {


    @Override
    public SkillType getType() {
        return SkillType.APP;
    }

    @Override
    public AppLauncherResponse execute(AppLauncherRequest request) {

        return switch (request.getOperation()) {
            case OPEN_APPLICATION -> openApp(request);
            case OPEN_FILE -> openFile(request);
            case OPEN_FOLDER -> openFolder(request);
        };
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

    private static final Map<String, String> APPLICATIONS = Map.of(
            "notepad", "notepad.exe",
            "calculator", "calc.exe",
            "paint", "mspaint.exe",
            "explorer", "explorer.exe",
            "vscode", "code.cmd",
            "intellij", "C:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2025.2.1\\bin\\idea64.exe",
            "cmd", "cmd.exe"
    );
}
