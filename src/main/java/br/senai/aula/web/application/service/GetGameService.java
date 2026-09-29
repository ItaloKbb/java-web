package br.senai.aula.web.application.service;

import br.senai.aula.web.application.port.in.GetGameUseCase;
import br.senai.aula.web.application.port.out.GameRepositoryPort;
import br.senai.aula.web.domain.game.Game;

import java.util.NoSuchElementException;

public class GetGameService implements GetGameUseCase {

    private final GameRepositoryPort gameRepositoryPort;

    public GetGameService(GameRepositoryPort gameRepositoryPort) {
        this.gameRepositoryPort = gameRepositoryPort;
    }

    @Override
    public Game getById(Long gameId) {
        return gameRepositoryPort.findById(gameId)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Jogo não encontrado: " + gameId
                        )
                );
    }
}
