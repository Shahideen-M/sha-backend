package com.sha.lifeafter.controller;

import com.sha.lifeafter.entity.LifeAfterCharacter;
import com.sha.lifeafter.service.LifeAfterCharacterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lifeafter")
@RequiredArgsConstructor
public class LifeAfterAccountController {

    private final LifeAfterCharacterService accountService;

    @PostMapping("/characters")
    public LifeAfterCharacter createAccount(@RequestBody LifeAfterCharacter account) {
        return accountService.createAccount(account);
    }

    @GetMapping("/characters")
    public List<LifeAfterCharacter> findAll() {
        return accountService.findAll();
    }

    @GetMapping("/characters/{id}")
    public LifeAfterCharacter findById(@PathVariable Long id) {
        return accountService.findById(id);
    }

    @PatchMapping("/characters/{id}/name")
    public LifeAfterCharacter update(@PathVariable Long id, @RequestBody LifeAfterCharacter character) {
        return accountService.updateCharacter(id, character);
    }

    @DeleteMapping("/characters/delete/{id}")
    public void delete(@PathVariable Long id) {
        accountService.deleteAccount(id);
    }
}
