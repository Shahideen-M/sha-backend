package com.sha.service.skills;

import com.sha.dto.request.FileRequest;
import com.sha.dto.response.FileResponse;
import com.sha.enums.SkillType;
import com.sha.service.Skill;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class FileSkill implements Skill<FileRequest, FileResponse> {

    @Override
    public SkillType getType() {
        return SkillType.FILE;
    }

    @Override
    public FileResponse execute(FileRequest request) {

        return switch (request.getOperation()) {

            case READ -> read(request);

            case WRITE -> write(request);

            case UPDATE -> update(request);

            case DELETE -> delete(request);

            case LIST -> list(request);

            case SEARCH -> search(request);

            default -> throw new RuntimeException("Unknown file operation");
        };
    }

    private Path getPath(FileRequest request) {
        return Path.of(request.getPath());
    }

    public FileResponse read(FileRequest request) {
        try {
            Path path = getPath(request);
            validateFile(path);

            String content = Files.readString(path);
            String fileName = path.getFileName().toString();

            int dotIndex = fileName.lastIndexOf('.');

            String extension = "";
            if (dotIndex > 0) {
                extension = fileName.substring(dotIndex + 1);
            }

            return new FileResponse(fileName, extension, content);

        } catch (IOException e) {
            throw new RuntimeException("Cannot read file: "+getPath(request),e);
        }
    }

    public FileResponse write(FileRequest request) {

        try {
            Path path = getPath(request);
            if (Files.exists(path)) throw new RuntimeException("File already exists with this name");

            Files.writeString(path, request.getContent());

            return new FileResponse(true, "File has been created at : " + path);

        } catch (IOException e) {
            throw new RuntimeException("Cannot create file: " + request.getPath(), e);
        }
    }

    public FileResponse update(FileRequest request) {

        try {
            Path path = getPath(request);
            validateFile(path);

            Files.writeString(path, request.getContent());

            return new FileResponse(true, "File has been updated successfully.");

        } catch (IOException e) {
            throw new RuntimeException("Cannot update file : " + request.getPath(), e);
        }
    }

    public FileResponse delete(FileRequest request) {

        try {
            Path path = getPath(request);
            validateFile(path);

            Files.delete(path);
            return new FileResponse(true, "File has been deleted successfully.");

        } catch (IOException e) {
            throw new RuntimeException("Cannot delete file: " + request.getPath(), e);
        }
    }

    public FileResponse list(FileRequest request) {

        try {
            Path path = getPath(request);
            validateDirectory(path);

            List<String> fileNames = Files.list(path)
                    .map(file -> file.getFileName().toString())
                    .toList();

            return new FileResponse(fileNames);

        } catch (IOException e) {
            throw new RuntimeException("Cannot list directory: " + request.getPath(), e);
        }
    }

    public FileResponse search(FileRequest request) {

        try {

            Path path = getPath(request);
            validateDirectory(path);

            List<String> matchingFiles = Files.list(path)
                    .map(file -> file.getFileName().toString())
                    .filter(name -> name.contains(request.getSearchKeyword()))
                    .toList();

            return new FileResponse(matchingFiles);

        } catch (IOException e) {
            throw new RuntimeException("Cannot search directory: " + request.getPath(), e);
        }
    }

    private void validateFile(Path path) {

        if (!Files.exists(path)) throw new RuntimeException("File not found: " + path);

        if (!Files.isRegularFile(path)) throw new RuntimeException("Not a file: " + path);

    }

    private void validateDirectory(Path path) {

        if (!Files.exists(path)) throw new RuntimeException("Directory not found: " + path);

        if (!Files.isDirectory(path)) throw new RuntimeException("Not a directory: " + path);

    }

}