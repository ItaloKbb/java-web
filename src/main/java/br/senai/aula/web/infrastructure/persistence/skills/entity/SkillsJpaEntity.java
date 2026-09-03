package br.senai.aula.web.infrastructure.persistence.skills.entity;

import br.senai.aula.web.domain.cards.Naipe;
import br.senai.aula.web.domain.skills.SkillType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "skills")
public class SkillsJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Naipe naipe;

    protected SkillsJpaEntity() {
    }

    public SkillsJpaEntity(Long id, String name, String description, SkillType type, Naipe naipe) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.naipe = naipe;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public SkillType getType() {
        return type;
    }

    public Naipe getNaipe() {
        return naipe;
    }
}