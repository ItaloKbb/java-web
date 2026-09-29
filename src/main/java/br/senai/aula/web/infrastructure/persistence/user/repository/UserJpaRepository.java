package br.senai.aula.web.infrastructure.persistence.user.repository;

import br.senai.aula.web.infrastructure.persistence.user.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    Optional<UserJpaEntity> findByNicknameIgnoreCase(String nickname);
    List<UserJpaEntity> findAllByOrderByRankingPointsDescNicknameAsc();
}
