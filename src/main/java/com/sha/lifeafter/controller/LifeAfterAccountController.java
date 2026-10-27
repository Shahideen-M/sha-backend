package com.sha.lifeafter.controller;

import com.sha.lifeafter.entity.LifeAfterAccount;
import com.sha.lifeafter.service.LifeAfterAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lifeafter")
@RequiredArgsConstructor
public class LifeAfterAccountController {

    private final LifeAfterAccountService accountService;

    @PostMapping("/accounts")
    public LifeAfterAccount createAccount(@RequestBody LifeAfterAccount account) {
        return accountService.createAccount(account);
    }

    @GetMapping("/accounts")
    public List<LifeAfterAccount> findAll() {
        return accountService.findAll();
    }

    @GetMapping("/accounts/{id}")
    public LifeAfterAccount findById(@PathVariable Long id) {
        return accountService.findById(id);
    }

    @PatchMapping("/accounts/{id}/name")
    public LifeAfterAccount update(@PathVariable Long id, @RequestBody String newName) {
        return accountService.renameAccount(id, newName);
    }

    @DeleteMapping("/accounts/delete/{id}")
    public void delete(@PathVariable Long id) {
        accountService.deleteAccount(id);
    }
}
