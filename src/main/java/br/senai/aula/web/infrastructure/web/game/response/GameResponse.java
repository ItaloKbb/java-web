package br.senai.aula.web.infrastructure.web.game.response;

import br.senai.aula.web.domain.game.Game;

public record GameResponse(
        Long id,
        String name,
        Integer maxPlayers
) {

    public static GameResponse from(Game game) {
        return new GameResponse(
                game.id(),
                game.name(),
                game.maxPlayers()
        );
    }
}