package br.senai.aula.web.application.service;

import br.senai.aula.web.application.port.in.FinishRoundUseCase;
import br.senai.aula.web.application.port.out.RoundRepositoryPort;
import br.senai.aula.web.domain.game.Round;
import br.senai.aula.web.domain.game.StatusRound;

import java.util.NoSuchElementException;

public class FinishRoundService implements FinishRoundUseCase {

    private final RoundRepositoryPort roundRepositoryPort;

    public FinishRoundService(RoundRepositoryPort roundRepositoryPort) {
        this.roundRepositoryPort = roundRepositoryPort;
    }

    @Override
    public Round finish(Long roundId) {
        Round round = roundRepositoryPort.findById(roundId)
                .orElseThrow(() -> new NoSuchElementException("Rodada não encontrada: " + roundId));

        if (round.status() == StatusRound.FINALIZADO) {
            throw new IllegalStateException("A rodada já foi finalizada.");
        }

        return roundRepositoryPort.updateStatus(roundId, StatusRound.FINALIZADO);
    }
}
