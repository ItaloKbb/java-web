package br.senai.aula.web.infrastructure.persistence.puzzle.mapper;

import br.senai.aula.web.domain.puzzle.Puzzle;
import br.senai.aula.web.infrastructure.persistence.puzzle.entity.PuzzleJpaEntity;

public final class PuzzlePersistenceMapper {

    private PuzzlePersistenceMapper() {
    }

    public static PuzzleJpaEntity toEntity(Puzzle domain) {
        return new PuzzleJpaEntity(domain.id(), domain.alternativas(), domain.alternativaCorreta());
    }

    public static Puzzle toDomain(PuzzleJpaEntity entity) {
        return new Puzzle(
                entity.getId(),
                entity.getAlternativas(),
                entity.getAlternativaCorreta()
        );
    }
}
