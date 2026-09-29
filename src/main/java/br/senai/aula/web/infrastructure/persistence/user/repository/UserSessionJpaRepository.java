package br.senai.aula.web.infrastructure.persistence.user.repository;

import br.senai.aula.web.infrastructure.persistence.user.entity.UserSessionJpaEntity;
import br.senai.aula.web.infrastructure.persistence.user.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

public interface UserSessionJpaRepository extends JpaRepository<UserSessionJpaEntity, Long> {
    @EntityGraph(attributePaths = "user")
    Optional<UserSessionJpaEntity> findByToken(String token);
    Optional<UserSessionJpaEntity> findByUser(UserJpaEntity user);
}
