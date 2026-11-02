package com.sha.lifeafter.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "lifeafter_items", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class LifeAfterItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
}