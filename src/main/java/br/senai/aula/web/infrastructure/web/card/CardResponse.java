package br.senai.aula.web.infrastructure.web.card;

import br.senai.aula.web.domain.cards.Card;
import br.senai.aula.web.domain.cards.Naipe;
import br.senai.aula.web.domain.cards.Valor;

public record CardResponse(
        Long id,
        Valor valor,
        Naipe naipe
) {

    public static CardResponse from(Card card) {
        return new CardResponse(card.id(), card.valor(), card.naipe());
    }
}
