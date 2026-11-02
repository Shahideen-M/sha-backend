package com.sha.lifeafter.service;

import com.sha.lifeafter.entity.LifeAfterItem;
import com.sha.lifeafter.repository.LifeAfterItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LifeAfterItemService {

    private final LifeAfterItemRepository repo;

    public LifeAfterItem createItem(LifeAfterItem item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item name is required");
        }
        if (repo.existsByName(item.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item name already exists");
        }
        return repo.save(item);
    }

    public List<LifeAfterItem> findAll() {
        return repo.findAll();
    }

    public LifeAfterItem findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
    }

    public LifeAfterItem updateItem(Long id, LifeAfterItem item) {
        LifeAfterItem existing = findById(id);

        if (item.getName() != null) {
            if (item.getName().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item name is required");
            }
            if (!item.getName().equals(existing.getName()) && repo.existsByName(item.getName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Item name already exists");
            }
            existing.setName(item.getName());
        }
        if (item.getCategory() != null) {
            existing.setCategory(item.getCategory());
        }

        return repo.save(existing);
    }

    public void deleteItem(Long id) {
        LifeAfterItem item = findById(id);
        repo.deleteById(item.getId());
    }
}