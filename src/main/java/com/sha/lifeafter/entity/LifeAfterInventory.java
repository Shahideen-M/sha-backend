package com.sha.lifeafter.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "lifeafter_inventory")
@Data
public class LifeAfterInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "character_id", nullable = false)
    private LifeAfterCharacter character;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private LifeAfterItem item;

    private Integer quantity;
}