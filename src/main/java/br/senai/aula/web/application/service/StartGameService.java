package br.senai.aula.web.application.service;

import br.senai.aula.web.application.port.in.StartGameUseCase;
import br.senai.aula.web.application.port.out.GameRepositoryPort;
import br.senai.aula.web.domain.game.Game;

public class StartGameService implements StartGameUseCase {

    private final GameRepositoryPort gameRepositoryPort;

    public StartGameService(GameRepositoryPort gameRepositoryPort) {
        this.gameRepositoryPort = gameRepositoryPort;
    }

    @Override
    public Game start(String name, Integer maxPlayers) {

        Game game = new Game(
                null,
                name,
                maxPlayers
        );

        return gameRepositoryPort.save(game);
    }
}