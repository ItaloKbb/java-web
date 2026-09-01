package br.senai.aula.web.infrastructure.persistence.game.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "deck")
public class DeckJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected DeckJpaEntity() {
    }

    public DeckJpaEntity(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
