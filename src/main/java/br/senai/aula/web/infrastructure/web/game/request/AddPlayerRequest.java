package br.senai.aula.web.infrastructure.web.game.request;

import jakarta.validation.constraints.NotNull;

public record AddPlayerRequest(
        @NotNull
        Long userId
) {
}
