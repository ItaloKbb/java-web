package br.senai.aula.web.infrastructure.web.game.response;

import br.senai.aula.web.domain.game.Deck;

public record DeckResponse(
        Long id
) {

    public static DeckResponse from(Deck deck) {
        return new DeckResponse(
                deck.id()
        );
    }
}