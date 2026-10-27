package com.sha.lifeafter.repository;

import com.sha.lifeafter.entity.LifeAfterAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LifeAfterAccountRepository extends JpaRepository<LifeAfterAccount, Long> {

    Optional<LifeAfterAccount> findByName(String name);
    boolean existsByName(String name);
}
