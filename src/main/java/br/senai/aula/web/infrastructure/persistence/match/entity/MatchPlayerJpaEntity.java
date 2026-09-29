package br.senai.aula.web.infrastructure.persistence.match.entity;

import br.senai.aula.web.infrastructure.persistence.user.entity.UserJpaEntity;
import jakarta.persistence.*;

@Entity
@Table(name="match_players", uniqueConstraints=@UniqueConstraint(columnNames={"game_id","user_id"}))
public class MatchPlayerJpaEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="game_id") private MatchGameJpaEntity game;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="user_id") private UserJpaEntity user;
    @Column(nullable=false) private Integer position;
    @Column(nullable=false) private Integer matchCoins=3;
    @Column(nullable=false) private Integer trophies=0;
    @Column(nullable=false) private Boolean ready=false;
    @Column(nullable=false) private Boolean trophyPurchased=false;
    @Column(nullable=false) private Integer skipTurns=0;
    @Column(nullable=false) private Integer shields=0;
    protected MatchPlayerJpaEntity() {}
    public MatchPlayerJpaEntity(MatchGameJpaEntity game, UserJpaEntity user, int position){this.game=game;this.user=user;this.position=position;}
    public Long getId(){return id;} public MatchGameJpaEntity getGame(){return game;} public UserJpaEntity getUser(){return user;}
    public Integer getPosition(){return position;} public Integer getMatchCoins(){return matchCoins;} public Integer getTrophies(){return trophies;}
    public Boolean getReady(){return ready;} public Boolean getTrophyPurchased(){return trophyPurchased;}
    public Integer getSkipTurns(){return skipTurns;} public Integer getShields(){return shields;}
    public void setPosition(int v){position=v;} public void addCoins(int v){matchCoins+=v;} public void spendCoins(int v){matchCoins-=v;}
    public void addTrophy(){trophies++; trophyPurchased=true;} public void setReady(boolean v){ready=v;}
    public void setTrophyPurchased(boolean v){trophyPurchased=v;} public void addSkipTurn(){skipTurns++;}
    public void consumeSkipTurn(){skipTurns--;} public void addShield(){shields++;} public void consumeShield(){shields--;}
}
