package br.senai.aula.web.application.port.in;

import br.senai.aula.web.domain.game.Game;

public interface StartGameUseCase {

    Game start(String name, Integer maxPlayers);
}