package br.senai.aula.web.infrastructure.web.game.response;

import br.senai.aula.web.domain.cards.Naipe;
import br.senai.aula.web.domain.cards.Valor;
import br.senai.aula.web.domain.match.Direction;
import br.senai.aula.web.domain.match.GamePhase;
import br.senai.aula.web.domain.match.RoundStatus;
import br.senai.aula.web.domain.skills.SkillType;
import java.util.List;

public record GameStateResponse(Long id,String code,String name,GamePhase phase,Long stateVersion,Settings settings,
 Direction direction,Integer roundNumber,RoundStatus roundStatus,CardView vira,Long currentPlayerId,List<PlayerView> players,
 List<PlayView> plays,List<CardView> hand,PuzzleView pendingPuzzle,Long winnerPlayerId) {
 public record Settings(Integer maxPlayers,Integer initialCards,Integer roundReward,Integer emptyHandReward,Integer trophyPrice){}
 public record PlayerView(Long id,String nickname,Integer position,Integer matchCoins,Integer trophies,Integer handSize,Boolean ready,Boolean host){}
 public record CardView(Long handCardId,Long catalogCardId,Valor valor,Naipe naipe,SkillType skill){}
 public record PlayView(Long playerId,String nickname,CardView card,Integer order){}
 public record PuzzleView(Long challengeId,String question,String[] alternatives){}
}
