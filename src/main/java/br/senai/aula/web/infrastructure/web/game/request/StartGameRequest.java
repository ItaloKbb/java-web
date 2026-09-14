package br.senai.aula.web.infrastructure.web.game.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StartGameRequest(

        @NotBlank
        String name,

        @NotNull
        @Min(2)
        @Max(6)
        Integer maxPlayers) {
}