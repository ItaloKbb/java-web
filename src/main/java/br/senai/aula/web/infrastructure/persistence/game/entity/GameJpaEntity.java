package br.senai.aula.web.infrastructure.persistence.user.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "games")
public class GameJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, default = 4)
    private Int maxPlayers;

    @OneToOne(mappedBy = "games", cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    private CoinJpaEntity coin;

    protected GameJpaEntity() {
    }

    public GameJpaEntity(Long id, String name, Int? maxPlayers) {
        this.id = id;
        this.name = name;
        this.maxPlayers = maxPlayers;
    }

    public void setCoin(CoinJpaEntity coin) {
        this.coin = coin;
        if (coin != null && coin.getUser() != this) {
            coin.setUser(this);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public CoinJpaEntity getCoin() {
        return coin;
    }
}
