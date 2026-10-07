package br.senai.aula.web.infrastructure.persistence.match.entity;
import br.senai.aula.web.domain.match.TheftKind;
import jakarta.persistence.*;
@Entity @Table(name="match_plays")
public class MatchPlayJpaEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="round_id") private MatchRoundJpaEntity round;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="player_id") private MatchPlayerJpaEntity player;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="match_card_id") private MatchCardJpaEntity card;
 @Column(nullable=false) private Integer playOrder;
 private Integer surpriseRoll;
 private Integer surpriseCoinDelta;
 private Integer buyCardsDrawn;
 @Enumerated(EnumType.STRING) private TheftKind theftKind;
 private Integer theftAmount;
 private Boolean theftBlocked;
 private Long theftTargetPlayerId;
 private Boolean puzzleCorrect;
 private Integer puzzleAmount;
 protected MatchPlayJpaEntity(){} public MatchPlayJpaEntity(MatchRoundJpaEntity r,MatchPlayerJpaEntity p,MatchCardJpaEntity c,int o){round=r;player=p;card=c;playOrder=o;}
 public Long getId(){return id;} public MatchRoundJpaEntity getRound(){return round;} public MatchPlayerJpaEntity getPlayer(){return player;} public MatchCardJpaEntity getCard(){return card;} public Integer getPlayOrder(){return playOrder;}
 public Integer getSurpriseRoll(){return surpriseRoll;} public Integer getSurpriseCoinDelta(){return surpriseCoinDelta;}
 public void recordSurprise(int roll,int coinDelta){surpriseRoll=roll;surpriseCoinDelta=coinDelta;}
 public Integer getBuyCardsDrawn(){return buyCardsDrawn;}
 public void recordBuyCardsDrawn(int amount){buyCardsDrawn=amount;}
 public TheftKind getTheftKind(){return theftKind;} public Integer getTheftAmount(){return theftAmount;}
 public Boolean getTheftBlocked(){return theftBlocked;} public Long getTheftTargetPlayerId(){return theftTargetPlayerId;}
 public void recordTheft(TheftKind kind,int amount,boolean blocked,long targetPlayerId){theftKind=kind;theftAmount=amount;theftBlocked=blocked;theftTargetPlayerId=targetPlayerId;}
 public Boolean getPuzzleCorrect(){return puzzleCorrect;} public Integer getPuzzleAmount(){return puzzleAmount;}
 public void recordPuzzle(boolean correct,int amount){puzzleCorrect=correct;puzzleAmount=amount;}
}
