package br.senai.aula.web.infrastructure.persistence.game.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import br.senai.aula.web.domain.game.StatusRound;

@Entity
@Table(name = "round")
public class RoundJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRound status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private GameJpaEntity game;

    protected RoundJpaEntity() {
    }

    public RoundJpaEntity(Long id, Integer number, StatusRound status, GameJpaEntity game) {
        this.id = id;
        this.number = number;
        this.status = status;
        this.game = game;
    }

    public Long getId() {
        return id;
    }

    public Integer getNumber() {
        return number;
    }

    public StatusRound getStatus() {
        return status;
    }

    public void setStatus(StatusRound status) {
        this.status = status;
    }

    public GameJpaEntity getGame() {
        return game;
    }
}
