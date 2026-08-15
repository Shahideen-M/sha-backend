package com.sha.service.skills;

import com.sha.brain.dto.OperationPrompt;
import com.sha.brain.prompt.SkillPrompt;
import com.sha.dto.request.FileRequest;
import com.sha.dto.response.FileResponse;
import com.sha.enums.FileOperation;
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
    public FileResponse executeTyped(FileRequest request) {

        return switch (request.getOperation()) {

            case READ -> read(request);

            case WRITE -> write(request);

            case UPDATE -> update(request);

            case DELETE -> delete(request);

            case LIST -> list(request);

            case SEARCH -> search(request);

            case COPY -> copy(request);

            case RENAME -> rename(request);

            default -> throw new RuntimeException("Unknown file operation");
        };
    }

    @Override
    public Class<FileRequest> getRequestClass() {
        return FileRequest.class;
    }

    @Override
    public FileResponse execute(Object request) {
        return executeTyped((FileRequest) request);
    }

    @Override
    public SkillPrompt<FileOperation> describe() {
        return new SkillPrompt<>(
                SkillType.FILE,
                "Read and manage files and directories.",
                List.of(
                        "file",
                        "read",
                        "write",
                        "create",
                        "update",
                        "delete",
                        "remove",
                        "list",
                        "search",
                        "copy",
                        "rename"
                ),
                List.of(
                        new OperationPrompt<>(
                                FileOperation.READ,
                                "Read a file.",
                                List.of("path"),
                                """
                                {
                                  "path":"D:\\Notes\\test.txt",
                                  "operation":"READ"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                FileOperation.WRITE,
                                "Create a new file.",
                                List.of("path", "content"),
                                """
                                {
                                  "path":"D:\\Notes\\test.txt",
                                  "content":"Hello Sha",
                                  "operation":"WRITE"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                FileOperation.UPDATE,
                                "Update an existing file.",
                                List.of("path", "content"),
                                """
                                {
                                  "path":"D:\\Notes\\test.txt",
                                  "content":"Updated content",
                                  "operation":"UPDATE"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                FileOperation.DELETE,
                                "Delete a file.",
                                List.of("path"),
                                """
                                {
                                  "path":"D:\\Notes\\test.txt",
                                  "operation":"DELETE"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                FileOperation.LIST,
                                "List files in a directory.",
                                List.of("path"),
                                """
                                {
                                  "path":"D:\\Projects",
                                  "operation":"LIST"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                FileOperation.SEARCH,
                                "Search files by name in a directory.",
                                List.of("path", "searchKeyword"),
                                """
                                {
                                  "path":"D:\\Projects",
                                  "searchKeyword":"Chat",
                                  "operation":"SEARCH"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                FileOperation.COPY,
                                "Copy a file.",
                                List.of("sourcePath", "destinationPath"),
                                """
                                {
                                  "sourcePath":"D:\\A.txt",
                                  "destinationPath":"D:\\Backup\\A.txt",
                                  "operation":"COPY"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                FileOperation.RENAME,
                                "Rename or move a file.",
                                List.of("sourcePath", "destinationPath"),
                                """
                                {
                                  "sourcePath":"D:\\A.txt",
                                  "destinationPath":"D:\\B.txt",
                                  "operation":"RENAME"
                                }
                                """
                        )
                )
        );
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

    public FileResponse copy(FileRequest request) {

        Path source = Path.of(request.getSourcePath());
        Path destination = Path.of(request.getDestinationPath());

        try {

            validateFile(source);
            validateDirectory(destination.getParent());
            validateNotExists(destination);

            Files.copy(source, destination);

            return new FileResponse(true, "Successfully copied");

        } catch (IOException e) {
            throw new RuntimeException("Cannot copy file from " + source + " to " + destination, e);
        }
    }

    public FileResponse rename(FileRequest request) {

        Path source = Path.of(request.getSourcePath());
        Path destination = Path.of(request.getDestinationPath());

        try {

            validateFile(source);
            validateDirectory(destination.getParent());
            validateNotExists(destination);

            Files.move(source, destination);

            return new FileResponse(true, "Successfully renamed");

        } catch (IOException e) {
            throw new RuntimeException("Cannot rename file from " + source + " to " + destination, e);
        }
    }

    private Path getPath(FileRequest request) {
        return Path.of(request.getPath());
    }

    private void validateFile(Path path) {

        if (!Files.exists(path)) throw new RuntimeException("File not found: " + path);

        if (!Files.isRegularFile(path)) throw new RuntimeException("Not a file: " + path);

    }

    private void validateDirectory(Path path) {

        if (!Files.exists(path)) throw new RuntimeException("Directory not found: " + path);

        if (!Files.isDirectory(path)) throw new RuntimeException("Not a directory: " + path);

    }

    private void validateNotExists(Path path) {

        if (Files.exists(path)) throw new RuntimeException("File already exists: " + path);

    }

}