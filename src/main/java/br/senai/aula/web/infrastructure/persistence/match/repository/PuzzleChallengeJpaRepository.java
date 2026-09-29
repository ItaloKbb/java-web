package br.senai.aula.web.infrastructure.persistence.match.repository;
import br.senai.aula.web.infrastructure.persistence.match.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PuzzleChallengeJpaRepository extends JpaRepository<PuzzleChallengeJpaEntity,Long>{Optional<PuzzleChallengeJpaEntity> findByIdAndGameAndAnsweredFalse(Long id,MatchGameJpaEntity game);Optional<PuzzleChallengeJpaEntity> findFirstByGameAndAnsweredFalse(MatchGameJpaEntity game);}
