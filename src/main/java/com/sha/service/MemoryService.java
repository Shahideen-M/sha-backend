package com.sha.service;

import com.sha.entity.Memory;
import com.sha.repo.MemoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemoryService {

    private final MemoryRepository memoryRepository;

    public MemoryService(MemoryRepository memoryRepository) {
        this.memoryRepository = memoryRepository;
    }

    public Memory save(Memory memory) {
        return memoryRepository.save(memory);
    }

    public Memory getById(Long id) {
        return memoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Memory not found"));
    }

    public List<Memory> getAll() {
        return memoryRepository.findAll();
    }

    public List<Memory> search(String query) {
        return memoryRepository.findByContentContainingIgnoreCase(query);
    }

    public void delete(Long id) {
        memoryRepository.deleteById(id);
    }
}
