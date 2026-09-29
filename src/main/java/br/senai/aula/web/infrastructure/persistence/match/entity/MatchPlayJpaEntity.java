package br.senai.aula.web.infrastructure.persistence.match.entity;
import jakarta.persistence.*;
@Entity @Table(name="match_plays")
public class MatchPlayJpaEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="round_id") private MatchRoundJpaEntity round;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="player_id") private MatchPlayerJpaEntity player;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="match_card_id") private MatchCardJpaEntity card;
 @Column(nullable=false) private Integer playOrder;
 protected MatchPlayJpaEntity(){} public MatchPlayJpaEntity(MatchRoundJpaEntity r,MatchPlayerJpaEntity p,MatchCardJpaEntity c,int o){round=r;player=p;card=c;playOrder=o;}
 public Long getId(){return id;} public MatchRoundJpaEntity getRound(){return round;} public MatchPlayerJpaEntity getPlayer(){return player;} public MatchCardJpaEntity getCard(){return card;} public Integer getPlayOrder(){return playOrder;}
}
