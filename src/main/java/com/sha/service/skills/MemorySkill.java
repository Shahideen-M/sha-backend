package com.sha.service.skills;

import com.sha.brain.dto.OperationPrompt;
import com.sha.brain.prompt.SkillPrompt;
import com.sha.dto.request.MemoryRequest;
import com.sha.dto.response.MemoryResponse;
import com.sha.entity.Memory;
import com.sha.enums.MemoryOperation;
import com.sha.brain.enums.SkillType;
import com.sha.service.MemoryService;
import com.sha.service.Skill;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemorySkill implements Skill<MemoryRequest, MemoryResponse> {

    private final MemoryService memoryService;

    public MemorySkill(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    @Override
    public SkillType getType() {
        return SkillType.MEMORY;
    }

    @Override
    public MemoryResponse executeTyped(MemoryRequest request) {
        return switch(request.getOperation()) {
            case SAVE -> save(request);
            case SEARCH -> search(request);
            case GET -> get(request);
            case DELETE -> delete(request);
        };
    }

    @Override
    public Class<MemoryRequest> getRequestClass() {
        return MemoryRequest.class;
    }

    @Override
    public MemoryResponse execute(Object request) {
        return executeTyped((MemoryRequest) request);
    }

    @Override
    public SkillPrompt<MemoryOperation> describe() {

        return new SkillPrompt<>(
                SkillType.MEMORY,
                "Store and retrieve persistent user memories.",
                List.of(
                        "memory",
                        "remember",
                        "forget",
                        "recall",
                        "save memory"
                ),
                List.of(

                        new OperationPrompt<>(
                                MemoryOperation.SAVE,
                                "Save information as a persistent memory.",
                                List.of("content", "category"),
                                """
                                {
                                  "content":"User prefers Java and Spring Boot.",
                                  "category":"preference",
                                  "operation":"SAVE"
                                }
                                """
                        ),

                        new OperationPrompt<>(
                                MemoryOperation.SEARCH,
                                "Search stored memories.",
                                List.of("query"),
                                """
                                {
                                  "query":"Java Spring Boot",
                                  "operation":"SEARCH"
                                }
                                """
                        ),

                        new OperationPrompt<>(
                                MemoryOperation.GET,
                                "Retrieve a memory by ID.",
                                List.of("id"),
                                """
                                {
                                  "id":1,
                                  "operation":"GET"
                                }
                                """
                        ),

                        new OperationPrompt<>(
                                MemoryOperation.DELETE,
                                "Delete a memory by ID.",
                                List.of("id"),
                                """
                                {
                                  "id":1,
                                  "operation":"DELETE"
                                }
                                """
                        )
                )
        );
    }

    public MemoryResponse save(MemoryRequest request) {

        Memory memory = new Memory();

        memory.setContent(request.getContent());
        memory.setCategory(request.getCategory());
        memory.setCreatedAt(LocalDateTime.now());
        memory.setUpdatedAt(LocalDateTime.now());

        Memory saved = memoryService.save(memory);

        return new MemoryResponse(
                true,
                "Memory saved successfully",
                saved,
                null
        );
    }

    public MemoryResponse search(MemoryRequest request) {
        List<Memory> memories = memoryService.search(request.getQuery());
        return new MemoryResponse(
                true,
                "Memory search completed.",
                null,
                memories
        );
    }

    public MemoryResponse get(MemoryRequest request) {

        Memory memory = memoryService.getById(request.getId());

        return new MemoryResponse(
                true,
                "Memory retrieved successfully.",
                memory,
                null
        );
    }

    public MemoryResponse delete(MemoryRequest request) {
        memoryService.delete(request.getId());
        return new MemoryResponse(
                true,
                "Memory deleted successfully",
                null,
                null
        );
    }

}
