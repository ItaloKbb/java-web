package br.senai.aula.web.infrastructure.web.game.response;

import br.senai.aula.web.domain.game.Round;
import br.senai.aula.web.domain.game.StatusRound;

public record RoundResponse(
        Long id,
        Integer number,
        StatusRound status
) {

    public static RoundResponse from(Round round) {
        return new RoundResponse(
                round.id(),
                round.number(),
                round.status()
        );
    }
}
