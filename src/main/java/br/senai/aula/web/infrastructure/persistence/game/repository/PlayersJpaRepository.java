package br.senai.aula.web.infrastructure.persistence.game.repository;

import br.senai.aula.web.infrastructure.persistence.game.entity.PlayersJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayersJpaRepository extends JpaRepository<PlayersJpaEntity, Long> {

    List<PlayersJpaEntity> findByGameId(Long gameId);

    long countByGameId(Long gameId);

    boolean existsByGameIdAndUserId(Long gameId, Long userId);
}
