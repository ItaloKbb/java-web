package br.senai.aula.web.application.service;

import br.senai.aula.web.application.port.in.AddPlayerToGameUseCase;
import br.senai.aula.web.application.port.out.GameRepositoryPort;
import br.senai.aula.web.application.port.out.PlayersRepositoryPort;
import br.senai.aula.web.domain.game.Game;
import br.senai.aula.web.domain.game.Players;

public class AddPlayerToGameService implements AddPlayerToGameUseCase {

    private final GameRepositoryPort gameRepositoryPort;
    private final PlayersRepositoryPort playersRepositoryPort;

    public AddPlayerToGameService(
            GameRepositoryPort gameRepositoryPort,
            PlayersRepositoryPort playersRepositoryPort
    ) {
        this.gameRepositoryPort = gameRepositoryPort;
        this.playersRepositoryPort = playersRepositoryPort;
    }

    @Override
    public Players addPlayer(Long gameId, Long userId) {

        Game game = gameRepositoryPort.findById(gameId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Jogo não encontrado: " + gameId
                        )
                );

        long playerCount =
                playersRepositoryPort.countByGameId(gameId);

        if (playerCount >= game.maxPlayers()) {
            throw new IllegalStateException(
                    "A partida já atingiu o limite de jogadores."
            );
        }

        if (playersRepositoryPort
                .existsByGameIdAndUserId(gameId, userId)) {

            throw new IllegalStateException(
                    "Este jogador já está na partida."
            );
        }

        return playersRepositoryPort.add(
                gameId,
                userId
        );
    }
}