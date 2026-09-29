package br.senai.aula.web.infrastructure.persistence.match.repository;
import br.senai.aula.web.infrastructure.persistence.match.entity.*;
import br.senai.aula.web.infrastructure.persistence.user.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.*;
public interface MatchPlayerJpaRepository extends JpaRepository<MatchPlayerJpaEntity,Long>{@EntityGraph(attributePaths="user") Optional<MatchPlayerJpaEntity> findByGameAndUser(MatchGameJpaEntity game,UserJpaEntity user);@EntityGraph(attributePaths="user") List<MatchPlayerJpaEntity> findByGameOrderByPosition(MatchGameJpaEntity game);long countByGame(MatchGameJpaEntity game);}
