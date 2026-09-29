package br.senai.aula.web.infrastructure.persistence.match.entity;
import br.senai.aula.web.infrastructure.persistence.puzzle.entity.PuzzleJpaEntity;
import jakarta.persistence.*;
@Entity @Table(name="puzzle_challenges")
public class PuzzleChallengeJpaEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="game_id") private MatchGameJpaEntity game;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="round_id") private MatchRoundJpaEntity round;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="player_id") private MatchPlayerJpaEntity player;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="puzzle_id") private PuzzleJpaEntity puzzle;
 @Column(nullable=false) private Boolean answered=false;
 protected PuzzleChallengeJpaEntity(){} public PuzzleChallengeJpaEntity(MatchGameJpaEntity g,MatchRoundJpaEntity r,MatchPlayerJpaEntity p,PuzzleJpaEntity z){game=g;round=r;player=p;puzzle=z;}
 public Long getId(){return id;} public MatchGameJpaEntity getGame(){return game;} public MatchRoundJpaEntity getRound(){return round;} public MatchPlayerJpaEntity getPlayer(){return player;} public PuzzleJpaEntity getPuzzle(){return puzzle;} public Boolean getAnswered(){return answered;} public void answer(){answered=true;}
}
