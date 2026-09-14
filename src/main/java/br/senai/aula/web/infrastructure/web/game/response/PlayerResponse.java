package br.senai.aula.web.infrastructure.web.game.response;

import br.senai.aula.web.domain.game.Players;

public record PlayerResponse(
        Long id,
        Long userId,
        String name,
        String email
) {

    public static PlayerResponse from(Players player) {
        return new PlayerResponse(
                player.id(),
                player.user().id(),
                player.user().name(),
                player.user().email()
        );
    }
}