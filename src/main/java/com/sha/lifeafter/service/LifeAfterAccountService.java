package com.sha.lifeafter.service;

import com.sha.lifeafter.entity.LifeAfterAccount;
import com.sha.lifeafter.repository.LifeAfterAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LifeAfterAccountService {

    private final LifeAfterAccountRepository repo;

    public LifeAfterAccount createAccount(LifeAfterAccount account) {
        if (repo.existsByName(account.getName())) {
            throw new RuntimeException("Account name already exists");
        }
        return repo.save(account);
    }

    public LifeAfterAccount findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Account not Found"));
    }

    public List<LifeAfterAccount> findAll() {
        return repo.findAll();
    }

    public LifeAfterAccount renameAccount(Long id, String newName) {
     LifeAfterAccount account = repo.findById(id)
             .orElseThrow(() -> new RuntimeException("Account not found"));
     if (!account.getName().equals(newName) && repo.existsByName(newName)) {
         throw new RuntimeException("Account name already exists");
     }
     account.setName(newName);
     return repo.save(account);
    }

    public void deleteAccount(Long id) {
        LifeAfterAccount account = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        repo.deleteById(id);
    }
}
