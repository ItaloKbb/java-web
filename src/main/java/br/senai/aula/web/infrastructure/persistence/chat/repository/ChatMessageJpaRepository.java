package br.senai.aula.web.infrastructure.persistence.chat.repository;

import br.senai.aula.web.infrastructure.persistence.chat.entity.ChatMessageJpaEntity;
import br.senai.aula.web.infrastructure.persistence.user.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessageJpaEntity, Long> {
    @EntityGraph(attributePaths = "user")
    List<ChatMessageJpaEntity> findTop50ByOrderByIdDesc();

    @EntityGraph(attributePaths = "user")
    List<ChatMessageJpaEntity> findTop100ByIdGreaterThanOrderByIdAsc(Long afterId);

    Optional<ChatMessageJpaEntity> findTopByUserOrderByIdDesc(UserJpaEntity user);
}
