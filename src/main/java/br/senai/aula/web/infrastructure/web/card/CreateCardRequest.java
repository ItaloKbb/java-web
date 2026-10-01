package br.senai.aula.web.infrastructure.web.card;

import br.senai.aula.web.domain.cards.Naipe;
import br.senai.aula.web.domain.cards.Valor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCardRequest(
        @NotNull Valor valor,
        @NotNull Naipe naipe,
        @NotBlank String url
) {
}
