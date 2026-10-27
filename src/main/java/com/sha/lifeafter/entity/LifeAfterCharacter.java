package com.sha.lifeafter.entity;

import com.sha.lifeafter.enums.Professions;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "lifeafter_characters", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class LifeAfterCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private Professions profession;
    private String server;
    private Long goldBars = 0L;
}
