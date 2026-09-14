package br.senai.aula.web.application.port.in;

import br.senai.aula.web.domain.game.Round;

public interface FinishRoundUseCase {

    Round finish(Long roundId);
}