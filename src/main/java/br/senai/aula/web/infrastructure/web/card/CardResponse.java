package br.senai.aula.web.infrastructure.web.card;

import br.senai.aula.web.domain.cards.Card;
import br.senai.aula.web.domain.cards.Naipe;
import br.senai.aula.web.domain.cards.Valor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CardResponse(
    @NotNull Long id,
    @NotNull Valor valor,
    @NotNull Naipe naipe,
    @NotBlank String url
) {

    public static CardResponse from(Card card) {
        return new CardResponse(card.id(), card.valor(), card.naipe(), card.url());
    }
}
