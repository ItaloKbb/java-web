package br.senai.aula.web.infrastructure.persistence.match.entity;

import br.senai.aula.web.domain.match.CardZone;
import br.senai.aula.web.infrastructure.persistence.card.entity.entity.CardEntity;
import jakarta.persistence.*;

@Entity
@Table(name="match_cards")
public class MatchCardJpaEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="game_id") private MatchGameJpaEntity game;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="card_id") private CardEntity card;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="owner_id") private MatchPlayerJpaEntity owner;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private CardZone zone;
    @Column(nullable=false) private Integer drawOrder;
    protected MatchCardJpaEntity() {}
    public MatchCardJpaEntity(MatchGameJpaEntity game, CardEntity card, int drawOrder){this.game=game;this.card=card;this.drawOrder=drawOrder;this.zone=CardZone.MONTE;}
    public Long getId(){return id;} public MatchGameJpaEntity getGame(){return game;} public CardEntity getCard(){return card;}
    public MatchPlayerJpaEntity getOwner(){return owner;} public CardZone getZone(){return zone;} public Integer getDrawOrder(){return drawOrder;}
    public void move(CardZone zone, MatchPlayerJpaEntity owner){this.zone=zone;this.owner=owner;}
    public void setDrawOrder(int value){drawOrder=value;}
}
