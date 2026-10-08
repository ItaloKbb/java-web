package br.senai.aula.web.infrastructure.persistence.match.entity;

import br.senai.aula.web.domain.match.Direction;
import br.senai.aula.web.domain.match.GamePhase;
import br.senai.aula.web.infrastructure.persistence.user.entity.UserJpaEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "match_games")
public class MatchGameJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Version private Long stateVersion;
    @Column(nullable=false, unique=true, length=6) private String accessCode;
    @Column(nullable=false) private String name;
    @Column(nullable=false) private Integer maxPlayers;
    @Column(nullable=false) private Integer initialCards;
    @Column(nullable=false) private Integer roundReward;
    @Column(nullable=false) private Integer emptyHandReward;
    @Column(nullable=false) private Integer trophyPrice;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private GamePhase phase = GamePhase.AGUARDANDO_JOGADORES;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Direction direction = Direction.HORARIO;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="host_id") private UserJpaEntity host;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="winner_id") private UserJpaEntity winner;
    private Integer roundNumber = 0;
    private Integer starterPosition = 0;
    private Integer currentPosition;
    private Integer resolvedTurns = 0;
    @Column(nullable=false) private Instant updatedAt = Instant.now();
    @OneToMany(mappedBy="game", cascade=CascadeType.ALL, orphanRemoval=true) @OrderBy("position asc")
    private List<MatchPlayerJpaEntity> players = new ArrayList<>();

    protected MatchGameJpaEntity() {}
    public MatchGameJpaEntity(String accessCode, String name, int maxPlayers, int initialCards, int roundReward,
                              int emptyHandReward, int trophyPrice, UserJpaEntity host) {
        this.accessCode=accessCode; this.name=name; this.maxPlayers=maxPlayers; this.initialCards=initialCards;
        this.roundReward=roundReward; this.emptyHandReward=emptyHandReward; this.trophyPrice=trophyPrice; this.host=host;
    }
    public void touch() { updatedAt=Instant.now(); }
    public Instant getUpdatedAt(){return updatedAt;}
    public Long getId(){return id;} public Long getStateVersion(){return stateVersion==null?0:stateVersion;}
    public String getAccessCode(){return accessCode;} public String getName(){return name;} public Integer getMaxPlayers(){return maxPlayers;}
    public Integer getInitialCards(){return initialCards;} public Integer getRoundReward(){return roundReward;}
    public Integer getEmptyHandReward(){return emptyHandReward;} public Integer getTrophyPrice(){return trophyPrice;}
    public GamePhase getPhase(){return phase;} public Direction getDirection(){return direction;} public UserJpaEntity getHost(){return host;}
    public UserJpaEntity getWinner(){return winner;} public Integer getRoundNumber(){return roundNumber;}
    public Integer getStarterPosition(){return starterPosition;} public Integer getCurrentPosition(){return currentPosition;}
    public Integer getResolvedTurns(){return resolvedTurns;} public List<MatchPlayerJpaEntity> getPlayers(){return players;}
    public void setPhase(GamePhase value){phase=value;} public void setDirection(Direction value){direction=value;}
    public void setWinner(UserJpaEntity value){winner=value;} public void setRoundNumber(int value){roundNumber=value;}
    public void setStarterPosition(int value){starterPosition=value;} public void setCurrentPosition(Integer value){currentPosition=value;}
    public void setResolvedTurns(int value){resolvedTurns=value;}
}
