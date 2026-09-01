package br.senai.aula.web.infrastructure.persistence.game.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "round")
public class RoundJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer number;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private GameJpaEntity game;

    protected RoundJpaEntity() {
    }

    public RoundJpaEntity(Long id, Integer number, GameJpaEntity game) {
        this.id = id;
        this.number = number;
        this.game = game;
    }

    public Long getId() {
        return id;
    }

    public Integer getNumber() {
        return number;
    }

    public GameJpaEntity getGame() {
        return game;
    }
}
