package br.senai.aula.web.application.port.in;

import br.senai.aula.web.domain.game.Game;

public interface GetGameUseCase {

    Game getById(Long gameId);
}