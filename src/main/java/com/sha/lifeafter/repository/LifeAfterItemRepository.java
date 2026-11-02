package com.sha.lifeafter.repository;

import com.sha.lifeafter.entity.LifeAfterItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LifeAfterItemRepository extends JpaRepository<LifeAfterItem, Long> {

    boolean existsByName(String name);
}