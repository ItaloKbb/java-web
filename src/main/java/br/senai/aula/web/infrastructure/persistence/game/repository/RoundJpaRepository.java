package br.senai.aula.web.infrastructure.persistence.game.repository;

import br.senai.aula.web.infrastructure.persistence.game.entity.RoundJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoundJpaRepository
        extends JpaRepository<RoundJpaEntity, Long> {

    long countByGameId(Long gameId);
}