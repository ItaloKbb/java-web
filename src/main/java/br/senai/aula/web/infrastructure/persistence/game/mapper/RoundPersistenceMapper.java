package br.senai.aula.web.infrastructure.persistence.game.mapper;

import br.senai.aula.web.domain.game.Round;
import br.senai.aula.web.infrastructure.persistence.game.entity.GameJpaEntity;
import br.senai.aula.web.infrastructure.persistence.game.entity.RoundJpaEntity;

public final class RoundPersistenceMapper {

    private RoundPersistenceMapper() {
    }

    public static RoundJpaEntity toEntity(
            Round round,
            GameJpaEntity game
    ) {
        return new RoundJpaEntity(
                round.id(),
                round.number(),
                round.status(),
                game);
    }

    public static Round toDomain(RoundJpaEntity entity) {
        return new Round(
                entity.getId(),
                entity.getNumber(),
                entity.getStatus()
        );
    }
}
