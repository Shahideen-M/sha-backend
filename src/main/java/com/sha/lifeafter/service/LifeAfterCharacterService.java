package com.sha.lifeafter.service;

import com.sha.lifeafter.entity.LifeAfterCharacter;
import com.sha.lifeafter.repository.LifeAfterCharacterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LifeAfterCharacterService {

    private final LifeAfterCharacterRepository repo;

    public LifeAfterCharacter createAccount(LifeAfterCharacter account) {
        if (repo.existsByName(account.getName())) {
            throw new RuntimeException("Account name already exists");
        }
        return repo.save(account);
    }

    public LifeAfterCharacter findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Account not Found"));
    }

    public List<LifeAfterCharacter> findAll() {
        return repo.findAll();
    }

    public LifeAfterCharacter updateCharacter(Long id, LifeAfterCharacter character) {
     LifeAfterCharacter account = repo.findById(id)
             .orElseThrow(() -> new RuntimeException("Account not found"));
     if (character.getName() != null) account.setName(character.getName());
     if (character.getProfession() != null) account.setProfession(character.getProfession());
     if (character.getGoldBars() != null) account.setGoldBars(character.getGoldBars());

     return repo.save(account);
    }

    public void deleteAccount(Long id) {
        LifeAfterCharacter account = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        repo.deleteById(id);
    }
}
