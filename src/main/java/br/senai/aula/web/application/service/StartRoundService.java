package br.senai.aula.web.application.service;

import br.senai.aula.web.application.port.in.StartRoundUseCase;
import br.senai.aula.web.application.port.out.GameRepositoryPort;
import br.senai.aula.web.application.port.out.RoundRepositoryPort;
import br.senai.aula.web.domain.game.Round;
import br.senai.aula.web.domain.game.StatusRound;

import java.util.NoSuchElementException;

public class StartRoundService implements StartRoundUseCase {

    private final GameRepositoryPort gameRepositoryPort;
    private final RoundRepositoryPort roundRepositoryPort;

    public StartRoundService(
            GameRepositoryPort gameRepositoryPort,
            RoundRepositoryPort roundRepositoryPort
    ) {
        this.gameRepositoryPort = gameRepositoryPort;
        this.roundRepositoryPort = roundRepositoryPort;
    }

    @Override
    public Round startRound(Long gameId) {

        if (gameRepositoryPort.findById(gameId).isEmpty()) {
            throw new NoSuchElementException(
                    "Jogo não encontrado: " + gameId
            );
        }

        long number =
                roundRepositoryPort.countByGameId(gameId) + 1;

        Round round = Round.newRound(
                Math.toIntExact(number),
                StatusRound.EM_ANDAMENTO
        );

        return roundRepositoryPort.save(
                round,
                gameId
        );
    }
}
