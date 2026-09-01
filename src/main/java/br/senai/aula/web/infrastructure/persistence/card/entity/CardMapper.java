package br.senai.aula.web.infrastructure.card;

import br.senai.aula.web.domain.cards.Card;

public class CardMapper {

    public static CardEntity toEntity(Card card) {

        return new CardEntity(
                card.id(),
                card.valor(),
                card.naipe()
        );
    }

    public static Card toDomain(CardEntity entity) {

        return new Card(
                entity.getId(),
                entity.getValor(),
                entity.getNaipe()
        );
    }

}