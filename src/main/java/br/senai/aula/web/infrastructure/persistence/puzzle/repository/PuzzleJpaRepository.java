package br.senai.aula.web.infrastructure.persistence.puzzle.repository;

import br.senai.aula.web.infrastructure.persistence.puzzle.entity.PuzzleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PuzzleJpaRepository extends JpaRepository<PuzzleJpaEntity, Long> {
}
