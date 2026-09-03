package br.senai.aula.web.infrastructure.persistence.skills.entity;

import br.senai.aula.web.domain.cards.Naipe;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Naipe naipe;

    protected SkillsJpaEntity() {
    }

    public SkillsJpaEntity(UUID id, String name, Naipe naipe) {
        this.id = id;
        this.name = name;
        this.naipe = naipe;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Naipe getNaipe() {
        return naipe;
    }
}