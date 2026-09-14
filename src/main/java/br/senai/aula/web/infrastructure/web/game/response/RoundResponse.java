package br.senai.aula.web.infrastructure.web.game.response;

import br.senai.aula.web.domain.game.Round;

public record RoundResponse(
        Long id,
        Integer number
) {

    public static RoundResponse from(Round round) {
        return new RoundResponse(
                round.id(),
                round.number()
        );
    }
}