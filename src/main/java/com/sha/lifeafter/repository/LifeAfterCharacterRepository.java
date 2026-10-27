package com.sha.lifeafter.repository;

import com.sha.lifeafter.entity.LifeAfterCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LifeAfterCharacterRepository extends JpaRepository<LifeAfterCharacter, Long> {

    Optional<LifeAfterCharacter> findByName(String name);
    boolean existsByName(String name);
}
