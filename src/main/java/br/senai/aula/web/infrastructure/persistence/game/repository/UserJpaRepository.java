package br.senai.aula.web.infrastructure.persistence.user.repository;

import br.senai.aula.web.infrastructure.persistence.user.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
}
