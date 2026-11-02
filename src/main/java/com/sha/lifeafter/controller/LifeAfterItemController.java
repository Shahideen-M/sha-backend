package com.sha.lifeafter.controller;

import com.sha.lifeafter.entity.LifeAfterItem;
import com.sha.lifeafter.service.LifeAfterItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lifeafter/items")
@RequiredArgsConstructor
public class LifeAfterItemController {

    private final LifeAfterItemService itemService;

    @PostMapping
    public LifeAfterItem createItem(@RequestBody LifeAfterItem item) {
        return itemService.createItem(item);
    }

    @GetMapping
    public List<LifeAfterItem> findAll() {
        return itemService.findAll();
    }

    @GetMapping("/{id}")
    public LifeAfterItem findById(@PathVariable Long id) {
        return itemService.findById(id);
    }

    @PatchMapping("/{id}")
    public LifeAfterItem update(@PathVariable Long id, @RequestBody LifeAfterItem item) {
        return itemService.updateItem(id, item);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        itemService.deleteItem(id);
    }
}