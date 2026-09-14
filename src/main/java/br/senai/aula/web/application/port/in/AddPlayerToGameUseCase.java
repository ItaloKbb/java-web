package br.senai.aula.web.application.port.in;

import br.senai.aula.web.domain.game.Players;

public interface AddPlayerToGameUseCase {

    Players addPlayer(Long gameId, Long userId);
}