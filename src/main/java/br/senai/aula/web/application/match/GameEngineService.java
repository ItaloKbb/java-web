package br.senai.aula.web.application.match;

import br.senai.aula.web.application.auth.ForbiddenException;
import br.senai.aula.web.domain.cards.Naipe;
import br.senai.aula.web.domain.cards.Valor;
import br.senai.aula.web.domain.match.*;
import br.senai.aula.web.domain.skills.SkillType;
import br.senai.aula.web.infrastructure.persistence.card.entity.entity.CardEntity;
import br.senai.aula.web.infrastructure.persistence.card.entity.repository.CardJpaRepository;
import br.senai.aula.web.infrastructure.persistence.match.entity.*;
import br.senai.aula.web.infrastructure.persistence.match.repository.*;
import br.senai.aula.web.infrastructure.persistence.puzzle.entity.PuzzleJpaEntity;
import br.senai.aula.web.infrastructure.persistence.puzzle.repository.PuzzleJpaRepository;
import br.senai.aula.web.infrastructure.persistence.skills.repository.SkillsJpaRepository;
import br.senai.aula.web.infrastructure.persistence.user.entity.UserJpaEntity;
import br.senai.aula.web.infrastructure.web.game.response.GameStateResponse;
import jakarta.persistence.OptimisticLockException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;

@Service
public class GameEngineService {
    private static final String CODE_CHARS="ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final List<Valor> VALUES=List.of(Valor.QUATRO,Valor.CINCO,Valor.SEIS,Valor.SETE,Valor.DAMA,Valor.VALETE,Valor.REI,Valor.AS,Valor.DOIS,Valor.TRES);
    private final MatchGameJpaRepository games; private final MatchPlayerJpaRepository players; private final MatchCardJpaRepository matchCards;
    private final MatchRoundJpaRepository rounds; private final MatchPlayJpaRepository plays; private final PuzzleChallengeJpaRepository challenges;
    private final CardJpaRepository catalog; private final SkillsJpaRepository skills; private final PuzzleJpaRepository puzzles;
    private final ApplicationEventPublisher events;
    private final SecureRandom random=new SecureRandom();
    public GameEngineService(MatchGameJpaRepository games,MatchPlayerJpaRepository players,MatchCardJpaRepository matchCards,
      MatchRoundJpaRepository rounds,MatchPlayJpaRepository plays,PuzzleChallengeJpaRepository challenges,CardJpaRepository catalog,
      SkillsJpaRepository skills,PuzzleJpaRepository puzzles,ApplicationEventPublisher events){this.games=games;this.players=players;this.matchCards=matchCards;this.rounds=rounds;this.plays=plays;this.challenges=challenges;this.catalog=catalog;this.skills=skills;this.puzzles=puzzles;this.events=events;}

    @Transactional
    public GameStateResponse create(UserJpaEntity user,String name,int maxPlayers,int initialCards,int roundReward,int emptyReward,int trophyPrice){
        validateSettings(maxPlayers,initialCards,roundReward,emptyReward,trophyPrice);
        MatchGameJpaEntity game=games.save(new MatchGameJpaEntity(uniqueCode(),name.trim(),maxPlayers,initialCards,roundReward,emptyReward,trophyPrice,user));
        players.save(new MatchPlayerJpaEntity(game,user,0)); touch(game); return state(game,user);
    }

    @Transactional
    public GameStateResponse access(UserJpaEntity user,String code){
        MatchGameJpaEntity game=games.findByAccessCodeIgnoreCase(code.trim()).orElseThrow(()->new NoSuchElementException("Jogo não encontrado"));
        Optional<MatchPlayerJpaEntity> existing=players.findByGameAndUser(game,user); if(existing.isPresent()) return state(game,user);
        requirePhase(game,GamePhase.AGUARDANDO_JOGADORES);
        long count=players.countByGame(game); if(count>=game.getMaxPlayers()) throw new IllegalStateException("O lobby está lotado");
        players.save(new MatchPlayerJpaEntity(game,user,(int)count)); touch(game); return state(game,user);
    }

    @Transactional
    public GameStateResponse start(UserJpaEntity user,long gameId){
        MatchGameJpaEntity game=requireGame(gameId); requireHost(game,user); requirePhase(game,GamePhase.AGUARDANDO_JOGADORES);
        List<MatchPlayerJpaEntity> ordered=playerList(game); if(ordered.size()<2) throw new IllegalStateException("São necessários pelo menos dois jogadores");
        if(ordered.size()*game.getInitialCards()+1>catalog.count()) throw new IllegalStateException("Não há cartas suficientes para distribuir as mãos e revelar a vira");
        Collections.shuffle(ordered,random); for(int i=0;i<ordered.size();i++) ordered.get(i).setPosition(i); players.saveAll(ordered);
        List<CardEntity> cards=new ArrayList<>(catalog.findAll()); if(cards.size()!=40) throw new IllegalStateException("O catálogo deve possuir 40 cartas");
        Collections.shuffle(cards,random); for(int i=0;i<cards.size();i++) matchCards.save(new MatchCardJpaEntity(game,cards.get(i),i));
        game.setPhase(GamePhase.EM_ANDAMENTO); game.setDirection(Direction.HORARIO); game.setStarterPosition(0);
        for(MatchPlayerJpaEntity player:ordered) draw(game,player,game.getInitialCards());
        beginRound(game,false); touch(game); return state(game,user);
    }

    @Transactional(readOnly=true)
    public GameStateResponse getState(UserJpaEntity user,long gameId){MatchGameJpaEntity game=requireGame(gameId); requireMember(game,user); return state(game,user);}

    @Transactional
    public GameStateResponse play(UserJpaEntity user,long gameId,long handCardId){
        MatchGameJpaEntity game=requireGame(gameId); requirePhase(game,GamePhase.EM_ANDAMENTO); MatchPlayerJpaEntity actor=requireMember(game,user);
        if(!Objects.equals(actor.getPosition(),game.getCurrentPosition())) throw new IllegalStateException("Não é o turno deste jogador");
        MatchRoundJpaEntity round=currentRound(game); if(round.getStatus()!=RoundStatus.EM_ANDAMENTO) throw new IllegalStateException("A rodada está aguardando a resposta do puzzle");
        if(plays.existsByRoundAndPlayer(round,actor)) throw new IllegalStateException("O jogador já realizou sua jogada nesta rodada");
        MatchCardJpaEntity card=matchCards.findByIdAndGame(handCardId,game).orElseThrow(()->new NoSuchElementException("Carta não encontrada"));
        if(card.getOwner()==null||!card.getOwner().getId().equals(actor.getId())||card.getZone()!=CardZone.MAO) throw new ForbiddenException("A carta não pertence à mão do jogador");
        card.move(CardZone.JOGADA,null); matchCards.save(card); int order=plays.findByRoundOrderByPlayOrder(round).size(); MatchPlayJpaEntity played=plays.save(new MatchPlayJpaEntity(round,actor,card,order));
        game.setResolvedTurns(game.getResolvedTurns()+1);
        refillIfEmpty(game,actor);
        SkillType skill=skill(card.getCard()); boolean waiting=applySkill(game,round,actor,skill,card.getCard().getNaipe(),played);
        refillEmptyHands(game);
        if(!waiting) advance(game,actor);
        touch(game); return state(game,user);
    }

    @Transactional
    public GameStateResponse answerPuzzle(UserJpaEntity user,long gameId,long challengeId,int alternativeIndex){
        MatchGameJpaEntity game=requireGame(gameId); requirePhase(game,GamePhase.EM_ANDAMENTO); MatchPlayerJpaEntity actor=requireMember(game,user);
        PuzzleChallengeJpaEntity challenge=challenges.findByIdAndGameAndAnsweredFalse(challengeId,game).orElseThrow(()->new NoSuchElementException("Desafio pendente não encontrado"));
        if(!challenge.getPlayer().getId().equals(actor.getId())) throw new ForbiddenException("Somente o jogador desafiado pode responder");
        if(alternativeIndex<0||alternativeIndex>=challenge.getPuzzle().getAlternativas().length) throw new IllegalArgumentException("Alternativa inválida");
        int power=puzzlePower(challenge.getSuit());boolean correct=challenge.getPuzzle().getAlternativaCorreta()==alternativeIndex;
        int amount=correct?power:draw(game,actor,power); if(correct) actor.addCoins(power);
        if(challenge.getPlay()!=null) challenge.getPlay().recordPuzzle(correct,amount);
        challenge.answer(); challenge.getRound().setStatus(RoundStatus.EM_ANDAMENTO); advance(game,actor); touch(game); return state(game,user);
    }

    @Transactional
    public GameStateResponse buyTrophy(UserJpaEntity user,long gameId){
        MatchGameJpaEntity game=requireGame(gameId); requirePhase(game,GamePhase.ENTRE_RODADAS); MatchPlayerJpaEntity player=requireMember(game,user);
        if(player.getReady()) throw new IllegalStateException("O jogador já confirmou esta janela");
        if(player.getTrophyPurchased()) throw new IllegalStateException("Só é permitido comprar um troféu por rodada");
        if(player.getMatchCoins()<game.getTrophyPrice()) throw new IllegalStateException("Moedas insuficientes");
        player.spendCoins(game.getTrophyPrice()); player.addTrophy();
        if(player.getTrophies()>=3){game.setWinner(player.getUser());game.setPhase(GamePhase.FINALIZADO);game.setCurrentPosition(null);player.getUser().addRankingPoints(10);}
        touch(game); return state(game,user);
    }

    @Transactional
    public GameStateResponse ready(UserJpaEntity user,long gameId){
        MatchGameJpaEntity game=requireGame(gameId); requirePhase(game,GamePhase.ENTRE_RODADAS); MatchPlayerJpaEntity player=requireMember(game,user);
        player.setReady(true); if(playerList(game).stream().allMatch(MatchPlayerJpaEntity::getReady)) beginRound(game,true);
        touch(game); return state(game,user);
    }

    @Transactional
    public GameStateResponse cancel(UserJpaEntity user,long gameId){MatchGameJpaEntity game=requireGame(gameId);requireHost(game,user);if(game.getPhase()==GamePhase.FINALIZADO)throw new IllegalStateException("A partida já foi finalizada");game.setPhase(GamePhase.CANCELADO);game.setCurrentPosition(null);touch(game);return state(game,user);}

    private boolean applySkill(MatchGameJpaEntity game,MatchRoundJpaEntity round,MatchPlayerJpaEntity actor,SkillType type,Naipe suit,MatchPlayJpaEntity played){
        if(type==null)return false;
        if(type==SkillType.INVERTS){game.setDirection(game.getDirection()==Direction.HORARIO?Direction.ANTI_HORARIO:Direction.HORARIO);return false;}
        MatchPlayerJpaEntity target=nextPlayer(game,actor.getPosition());
        switch(type){
            case BLOCK -> {if(!blocked(target))target.addSkipTurn();}
            case THEFT -> applyTheft(game,actor,target,suit,played);
            case BUY -> played.recordBuyCardsDrawn(blocked(target)?0:draw(game,target,buyPower(suit)));
            case BURN -> {List<MatchCardJpaEntity> hand=hand(game,actor);if(!hand.isEmpty())hand.get(random.nextInt(hand.size())).move(CardZone.DESCARTE,null);}
            case SURPRISE -> {
                int roll=surprisePower(suit)*(random.nextBoolean()?1:-1);
                int coinDelta=surpriseCoinDelta(actor.getMatchCoins(),roll);
                actor.addCoins(coinDelta);
                played.recordSurprise(roll,coinDelta);
            }
            case PUZZLE -> {
                List<PuzzleJpaEntity> active=puzzles.findByArchivedAtIsNull();
                PuzzleJpaEntity puzzle=selectPuzzle(active,challenges.findPuzzleIdsByGame(game),random);
                challenges.save(new PuzzleChallengeJpaEntity(game,round,actor,puzzle,played,suit));
                round.setStatus(RoundStatus.AGUARDANDO_PUZZLE);
                return true;
            }
            case CHANGEOFHANDS -> {if(!blocked(target)){List<MatchCardJpaEntity> own=hand(game,actor), other=hand(game,target);own.forEach(c->c.move(CardZone.MAO,target));other.forEach(c->c.move(CardZone.MAO,actor));}}
            case BOMB -> {for(MatchPlayerJpaEntity opponent:playerList(game))if(!opponent.getId().equals(actor.getId())&&!blocked(opponent))draw(game,opponent,1);}
            case SHIELD -> actor.addShield();
            default -> { }
        }
        return false;
    }

    static int surprisePower(Naipe suit){return switch(suit){case OUROS->1;case ESPADAS->2;case COPAS->3;case PAUS->4;};}
    static int surpriseCoinDelta(int currentCoins,int roll){return Math.max(-Math.max(0,currentCoins),roll);}
    static int buyPower(Naipe suit){return switch(suit){case OUROS,ESPADAS->2;case COPAS->3;case PAUS->4;};}
    static int puzzlePower(Naipe suit){return surprisePower(suit);}
    static PuzzleJpaEntity selectPuzzle(List<PuzzleJpaEntity> active,List<Long> history,Random random){
        if(active.isEmpty())throw new IllegalStateException("Nenhum puzzle cadastrado");
        Set<Long> activeIds=new HashSet<>();
        active.forEach(puzzle->activeIds.add(puzzle.getId()));
        Set<Long> usedThisCycle=new HashSet<>();
        for(Long puzzleId:history){
            if(!activeIds.contains(puzzleId))continue;
            usedThisCycle.add(puzzleId);
            if(usedThisCycle.size()==activeIds.size())usedThisCycle.clear();
        }
        List<PuzzleJpaEntity> unseen=active.stream().filter(puzzle->!usedThisCycle.contains(puzzle.getId())).toList();
        return unseen.get(random.nextInt(unseen.size()));
    }
    static int theftPower(Naipe suit){return switch(suit){case OUROS,ESPADAS->2;case COPAS,PAUS->3;};}
    static int theftAmount(int available,int power){return Math.min(power,Math.max(0,available));}

    private void applyTheft(MatchGameJpaEntity game,MatchPlayerJpaEntity actor,MatchPlayerJpaEntity target,Naipe suit,MatchPlayJpaEntity played){
        TheftKind kind=random.nextBoolean()?TheftKind.CARD:TheftKind.COIN;
        boolean shielded=blocked(target);
        int amount=0;
        if(!shielded){
            int power=theftPower(suit);
            if(kind==TheftKind.CARD){
                List<MatchCardJpaEntity> targetHand=hand(game,target);
                amount=theftAmount(targetHand.size(),power);
                Collections.shuffle(targetHand,random);
                for(int i=0;i<amount;i++)targetHand.get(i).move(CardZone.MAO,actor);
            }else{
                amount=theftAmount(target.getMatchCoins(),power);
                target.spendCoins(amount);
                actor.addCoins(amount);
            }
        }
        played.recordTheft(kind,amount,shielded,target.getId());
    }

    private boolean blocked(MatchPlayerJpaEntity target){if(target.getShields()>0){target.consumeShield();return true;}return false;}
    private void advance(MatchGameJpaEntity game,MatchPlayerJpaEntity actor){
        if(game.getResolvedTurns()>=playerList(game).size()){finishRound(game);return;}
        MatchRoundJpaEntity round=currentRound(game); MatchPlayerJpaEntity candidate=nextPlayer(game,actor.getPosition()); int guard=0;
        while(guard++<playerList(game).size()*2){
            if(plays.existsByRoundAndPlayer(round,candidate)){candidate=nextPlayer(game,candidate.getPosition());continue;}
            if(candidate.getSkipTurns()>0){candidate.consumeSkipTurn();game.setResolvedTurns(game.getResolvedTurns()+1);if(game.getResolvedTurns()>=playerList(game).size()){finishRound(game);return;}candidate=nextPlayer(game,candidate.getPosition());continue;}
            game.setCurrentPosition(candidate.getPosition());return;
        }
        finishRound(game);
    }

    private void finishRound(MatchGameJpaEntity game){
        MatchRoundJpaEntity round=currentRound(game); List<MatchPlayJpaEntity> roundPlays=plays.findByRoundOrderByPlayOrder(round);
        int best=Integer.MIN_VALUE; List<MatchPlayJpaEntity> winners=new ArrayList<>();
        for(MatchPlayJpaEntity play:roundPlays){int strength=strength(play.getCard().getCard(),round.getVira().getCard());if(strength>best){best=strength;winners.clear();winners.add(play);}else if(strength==best)winners.add(play);}
        if(winners.size()==1){MatchPlayerJpaEntity winner=winners.get(0).getPlayer();winner.addCoins(game.getRoundReward());round.setWinner(winner);}
        roundPlays.forEach(p->p.getCard().move(CardZone.DESCARTE,null));round.getVira().move(CardZone.DESCARTE,null);round.setStatus(RoundStatus.FINALIZADO);
        for(MatchPlayerJpaEntity p:playerList(game)){p.setReady(false);p.setTrophyPurchased(false);}
        game.setPhase(GamePhase.ENTRE_RODADAS);game.setCurrentPosition(null);
    }

    private int strength(CardEntity card,CardEntity vira){Valor manilha=VALUES.get((VALUES.indexOf(vira.getValor())+1)%VALUES.size());if(card.getValor()==manilha)return 100+suitStrength(card.getNaipe());return VALUES.indexOf(card.getValor());}
    private int suitStrength(Naipe suit){return switch(suit){case OUROS->0;case ESPADAS->1;case COPAS->2;case PAUS->3;};}
    private void beginRound(MatchGameJpaEntity game,boolean advanceStarter){
        List<MatchPlayerJpaEntity> ps=playerList(game);if(advanceStarter)game.setStarterPosition(positionAfter(game,game.getStarterPosition()));
        for(MatchPlayerJpaEntity p:ps){p.setReady(false);p.setTrophyPurchased(false);}
        MatchCardJpaEntity vira=drawOne(game);if(vira==null)throw new IllegalStateException("Não há carta disponível para a vira");vira.move(CardZone.VIRA,null);
        int number=game.getRoundNumber()+1;rounds.save(new MatchRoundJpaEntity(game,number,vira));game.setRoundNumber(number);game.setResolvedTurns(0);game.setCurrentPosition(game.getStarterPosition());game.setPhase(GamePhase.EM_ANDAMENTO);
    }

    private void refillEmptyHands(MatchGameJpaEntity game){for(MatchPlayerJpaEntity p:playerList(game))refillIfEmpty(game,p);}
    private void refillIfEmpty(MatchGameJpaEntity game,MatchPlayerJpaEntity player){if(hand(game,player).isEmpty()){player.addCoins(game.getEmptyHandReward());draw(game,player,game.getInitialCards());}}
    private int draw(MatchGameJpaEntity game,MatchPlayerJpaEntity player,int amount){int drawn=0;for(int i=0;i<amount;i++){MatchCardJpaEntity card=drawOne(game);if(card==null)break;card.move(CardZone.MAO,player);drawn++;}return drawn;}
    private MatchCardJpaEntity drawOne(MatchGameJpaEntity game){
        List<MatchCardJpaEntity> deck=matchCards.findByGameAndZoneOrderByDrawOrder(game,CardZone.MONTE);if(deck.isEmpty()){
            List<MatchCardJpaEntity> discard=matchCards.findByGameAndZoneOrderByDrawOrder(game,CardZone.DESCARTE);Collections.shuffle(discard,random);for(int i=0;i<discard.size();i++){discard.get(i).move(CardZone.MONTE,null);discard.get(i).setDrawOrder(i);}matchCards.saveAll(discard);deck=discard;
        }return deck.isEmpty()?null:deck.get(0);
    }

    private MatchPlayerJpaEntity nextPlayer(MatchGameJpaEntity game,int from){int position=positionAfter(game,from);return playerList(game).stream().filter(p->p.getPosition()==position).findFirst().orElseThrow();}
    private int positionAfter(MatchGameJpaEntity game,int from){int size=playerList(game).size();return game.getDirection()==Direction.HORARIO?(from+1)%size:(from-1+size)%size;}
    private List<MatchPlayerJpaEntity> playerList(MatchGameJpaEntity game){return players.findByGameOrderByPosition(game);}
    private List<MatchCardJpaEntity> hand(MatchGameJpaEntity game,MatchPlayerJpaEntity player){return matchCards.findByGameAndOwnerAndZone(game,player,CardZone.MAO);}
    private MatchRoundJpaEntity currentRound(MatchGameJpaEntity game){return rounds.findFirstByGameOrderByNumberDesc(game).orElseThrow(()->new IllegalStateException("A partida ainda não possui rodada"));}
    private SkillType skill(CardEntity card){return skills.findByValorAndNaipeAndArchivedAtIsNull(card.getValor(),card.getNaipe()).map(s->s.getType()).orElse(null);}
    private MatchGameJpaEntity requireGame(long id){return games.findById(id).orElseThrow(()->new NoSuchElementException("Jogo não encontrado"));}
    private MatchPlayerJpaEntity requireMember(MatchGameJpaEntity game,UserJpaEntity user){return players.findByGameAndUser(game,user).orElseThrow(()->new ForbiddenException("O usuário não participa deste jogo"));}
    private void requireHost(MatchGameJpaEntity game,UserJpaEntity user){if(!game.getHost().getId().equals(user.getId()))throw new ForbiddenException("Somente o host pode executar esta ação");}
    private void requirePhase(MatchGameJpaEntity game,GamePhase expected){if(game.getPhase()!=expected)throw new IllegalStateException("Ação indisponível na fase "+game.getPhase());}
    private void touch(MatchGameJpaEntity game){game.touch();games.saveAndFlush(game);events.publishEvent(new GameStateChangedEvent(game.getId()));}
    private void validateSettings(int max,int initial,int reward,int empty,int trophy){if(max<2||max>6)throw new IllegalArgumentException("maxPlayers deve estar entre 2 e 6");if(initial<1||initial>10)throw new IllegalArgumentException("initialCards deve estar entre 1 e 10");if(reward<0||empty<0||trophy<1)throw new IllegalArgumentException("Recompensas não podem ser negativas e o troféu deve custar ao menos 1");}
    private String uniqueCode(){String code;do{StringBuilder b=new StringBuilder();for(int i=0;i<6;i++)b.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));code=b.toString();}while(games.existsByAccessCode(code));return code;}

    private GameStateResponse state(MatchGameJpaEntity game,UserJpaEntity viewer){
        MatchPlayerJpaEntity self=requireMember(game,viewer);List<MatchPlayerJpaEntity> ps=playerList(game);MatchRoundJpaEntity round=rounds.findFirstByGameOrderByNumberDesc(game).orElse(null);
        GameStateResponse.CardView vira=round==null?null:cardView(round.getVira(),false);List<GameStateResponse.PlayView> open=round==null?List.of():plays.findByRoundOrderByPlayOrder(round).stream().map(p->new GameStateResponse.PlayView(p.getPlayer().getId(),p.getPlayer().getUser().getNickname(),cardView(p.getCard(),false),p.getPlayOrder(),p.getSurpriseRoll(),p.getSurpriseCoinDelta(),p.getBuyCardsDrawn(),p.getTheftKind(),p.getTheftAmount(),p.getTheftBlocked(),p.getTheftTargetPlayerId(),p.getPuzzleCorrect(),p.getPuzzleAmount())).toList();
        List<GameStateResponse.PlayerView> views=ps.stream().map(p->new GameStateResponse.PlayerView(p.getId(),p.getUser().getNickname(),p.getPosition(),p.getMatchCoins(),p.getTrophies(),hand(game,p).size(),p.getReady(),p.getUser().getId().equals(game.getHost().getId()))).toList();
        List<GameStateResponse.CardView> own=hand(game,self).stream().map(c->cardView(c,true)).toList();
        GameStateResponse.PuzzleView puzzle=challenges.findFirstByGameAndAnsweredFalse(game).map(c->new GameStateResponse.PuzzleView(c.getId(),c.getPuzzle().getQuestion(),c.getPuzzle().getAlternativas())).orElse(null);
        Long winnerId=game.getWinner()==null?null:ps.stream().filter(p->p.getUser().getId().equals(game.getWinner().getId())).map(MatchPlayerJpaEntity::getId).findFirst().orElse(null);
        List<GameStateResponse.RoundWinnerView> roundWinners=rounds.findByGameAndStatusOrderByNumberAsc(game,RoundStatus.FINALIZADO).stream().map(r->r.getWinner()==null?new GameStateResponse.RoundWinnerView(r.getNumber(),null,null):new GameStateResponse.RoundWinnerView(r.getNumber(),r.getWinner().getId(),r.getWinner().getUser().getNickname())).toList();
        return new GameStateResponse(game.getId(),game.getAccessCode(),game.getName(),game.getPhase(),game.getStateVersion(),new GameStateResponse.Settings(game.getMaxPlayers(),game.getInitialCards(),game.getRoundReward(),game.getEmptyHandReward(),game.getTrophyPrice()),game.getDirection(),game.getRoundNumber(),round==null?null:round.getStatus(),vira,game.getCurrentPosition()==null?null:ps.stream().filter(p->p.getPosition().equals(game.getCurrentPosition())).map(MatchPlayerJpaEntity::getId).findFirst().orElse(null),views,open,own,puzzle,winnerId,roundWinners);
    }
    private GameStateResponse.CardView cardView(MatchCardJpaEntity card,boolean exposeId){return new GameStateResponse.CardView(exposeId?card.getId():null,card.getCard().getId(),card.getCard().getValor(),card.getCard().getNaipe(),skill(card.getCard()));}
}
