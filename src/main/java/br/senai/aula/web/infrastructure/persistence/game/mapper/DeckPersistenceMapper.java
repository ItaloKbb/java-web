package br.senai.aula.web.infrastructure.persistence.game.mapper;

import br.senai.aula.web.domain.game.Deck;
import br.senai.aula.web.infrastructure.persistence.game.entity.DeckJpaEntity;

public final class DeckPersistenceMapper {

    private DeckPersistenceMapper() {
    }

    public static DeckJpaEntity toEntity(Deck deck) {
        return new DeckJpaEntity(
                deck.id()
        );
    }

    public static Deck toDomain(DeckJpaEntity entity) {
        return new Deck(
                entity.getId()
        );
    }
}