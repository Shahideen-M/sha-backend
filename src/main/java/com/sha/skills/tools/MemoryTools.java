package com.sha.skills.tools;

import com.sha.skills.MemorySkill;
import com.sha.skills.Skill;
import com.sha.skills.dto.request.MemoryRequest;
import com.sha.skills.dto.response.MemoryResponse;
import com.sha.skills.enums.MemoryOperation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Getter
@Component
@RequiredArgsConstructor
public class MemoryTools implements ShaTool {

    private final MemorySkill memorySkill;
    private final ObjectMapper objectMapper;

    @Override
    public Skill<?, ?> getSkill() {
        return memorySkill;
    }

    @Override
    public Object createRequest(String toolName, String arguments) {

        MemoryRequest request = new MemoryRequest();
        var json = objectMapper.readTree(arguments);

        switch (toolName) {
            case "saveMemory" -> {
                request.setContent(json.get("content").asString());
                request.setCategory(json.hasNonNull("category") ? json.get("category").asString() : null);
                request.setOperation(MemoryOperation.SAVE);
            }
            case "searchMemory" -> {
                request.setQuery(json.get("query").asString());
                request.setOperation(MemoryOperation.SEARCH);
            }
            case "getMemory" -> {
                request.setId(json.get("id").asLong());
                request.setOperation(MemoryOperation.GET);
            }
            case "updateMemory" -> {
                request.setId(json.get("id").asLong());
                request.setContent(json.hasNonNull("content") ? json.get("content").asString() : null);
                request.setCategory(json.hasNonNull("category") ? json.get("category").asString() : null);
                request.setOperation(MemoryOperation.UPDATE);
            }
            case "deleteMemory" -> {
                request.setId(json.get("id").asLong());
                request.setOperation(MemoryOperation.DELETE);
            }
            default -> throw new IllegalArgumentException("Unknown MemoryTools operation: " + toolName);
        }
        return request;
    }

    @Tool(description = "Save information as a persistent memory")
    public MemoryResponse saveMemory(String content, String category) {
        MemoryRequest request = new MemoryRequest();
        request.setContent(content);
        request.setCategory(category);
        request.setOperation(MemoryOperation.SAVE);
        return memorySkill.execute(request);
    }

    @Tool(description = "Search stored memories")
    public MemoryResponse searchMemory(String query) {
        MemoryRequest request = new MemoryRequest();
        request.setQuery(query);
        request.setOperation(MemoryOperation.SEARCH);
        return memorySkill.execute(request);
    }

    @Tool(description = "Retrieve a stored memory by ID")
    public MemoryResponse getMemory(long id) {
        MemoryRequest request = new MemoryRequest();
        request.setId(id);
        request.setOperation(MemoryOperation.GET);
        return memorySkill.execute(request);
    }

    @Tool(description = "Update an existing memory by ID")
    public MemoryResponse updateMemory(long id, String content, String category) {
        MemoryRequest request = new MemoryRequest();
        request.setId(id);
        request.setContent(content);
        request.setCategory(category);
        request.setOperation(MemoryOperation.UPDATE);
        return memorySkill.execute(request);
    }

    @Tool(description = "Delete a stored memory by ID")
    public MemoryResponse deleteMemory(long id) {
        MemoryRequest request = new MemoryRequest();
        request.setId(id);
        request.setOperation(MemoryOperation.DELETE);
        return memorySkill.execute(request);
    }
}
