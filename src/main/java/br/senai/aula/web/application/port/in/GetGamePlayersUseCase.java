package br.senai.aula.web.application.port.in;

import br.senai.aula.web.domain.game.Players;

import java.util.List;

public interface GetGamePlayersUseCase {

    List<Players> getPlayers(Long gameId);
}