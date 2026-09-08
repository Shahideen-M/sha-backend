package com.sha.skills.repo;

import com.sha.skills.entity.Memory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemoryRepository extends JpaRepository<Memory, Long> {

    List<Memory> findByContentContainingIgnoreCase(String query);

}
