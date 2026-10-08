package br.senai.aula.web.infrastructure.persistence.match.repository;

import br.senai.aula.web.infrastructure.persistence.match.entity.MatchGameJpaEntity;
import br.senai.aula.web.infrastructure.persistence.match.entity.PuzzleChallengeJpaEntity;
import br.senai.aula.web.infrastructure.persistence.puzzle.entity.PuzzleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PuzzleChallengeJpaRepository extends JpaRepository<PuzzleChallengeJpaEntity, Long> {
    Optional<PuzzleChallengeJpaEntity> findByIdAndGameAndAnsweredFalse(Long id, MatchGameJpaEntity game);

    Optional<PuzzleChallengeJpaEntity> findFirstByGameAndAnsweredFalse(MatchGameJpaEntity game);

    boolean existsByPuzzleAndAnsweredFalse(PuzzleJpaEntity puzzle);

    @Query("select challenge.puzzle.id from PuzzleChallengeJpaEntity challenge where challenge.game = :game order by challenge.id")
    List<Long> findPuzzleIdsByGame(@Param("game") MatchGameJpaEntity game);
}
