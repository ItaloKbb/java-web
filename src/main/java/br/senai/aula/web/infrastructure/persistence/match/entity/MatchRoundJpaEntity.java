package br.senai.aula.web.infrastructure.persistence.match.entity;

import br.senai.aula.web.domain.match.RoundStatus;
import jakarta.persistence.*;

@Entity
@Table(name="match_rounds", uniqueConstraints=@UniqueConstraint(columnNames={"game_id","number"}))
public class MatchRoundJpaEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="game_id") private MatchGameJpaEntity game;
    @Column(nullable=false) private Integer number;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private RoundStatus status;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="vira_id") private MatchCardJpaEntity vira;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="winner_player_id") private MatchPlayerJpaEntity winner;
    protected MatchRoundJpaEntity() {}
    public MatchRoundJpaEntity(MatchGameJpaEntity game,int number,MatchCardJpaEntity vira){this.game=game;this.number=number;this.vira=vira;this.status=RoundStatus.EM_ANDAMENTO;}
    public Long getId(){return id;} public MatchGameJpaEntity getGame(){return game;} public Integer getNumber(){return number;}
    public RoundStatus getStatus(){return status;} public MatchCardJpaEntity getVira(){return vira;} public MatchPlayerJpaEntity getWinner(){return winner;}
    public void setStatus(RoundStatus v){status=v;} public void setWinner(MatchPlayerJpaEntity v){winner=v;}
}
