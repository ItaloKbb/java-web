package br.senai.aula.web.application.service;

import br.senai.aula.web.application.port.in.GetRoundUseCase;
import br.senai.aula.web.application.port.out.RoundRepositoryPort;
import br.senai.aula.web.domain.game.Round;

public class GetRoundService implements GetRoundUseCase {

    private final RoundRepositoryPort roundRepositoryPort;

    public GetRoundService(
            RoundRepositoryPort roundRepositoryPort
    ) {
        this.roundRepositoryPort = roundRepositoryPort;
    }

    @Override
    public Round getById(Long roundId) {

        return roundRepositoryPort.findById(roundId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Rodada não encontrada: " + roundId
                        )
                );
    }
}