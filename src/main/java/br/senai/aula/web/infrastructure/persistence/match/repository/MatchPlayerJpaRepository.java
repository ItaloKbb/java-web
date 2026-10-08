package br.senai.aula.web.infrastructure.persistence.match.repository;
import br.senai.aula.web.domain.match.GamePhase;
import br.senai.aula.web.infrastructure.persistence.match.entity.*;
import br.senai.aula.web.infrastructure.persistence.user.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface MatchPlayerJpaRepository extends JpaRepository<MatchPlayerJpaEntity,Long>{@EntityGraph(attributePaths="user") Optional<MatchPlayerJpaEntity> findByGameAndUser(MatchGameJpaEntity game,UserJpaEntity user);@EntityGraph(attributePaths="user") List<MatchPlayerJpaEntity> findByGameOrderByPosition(MatchGameJpaEntity game);long countByGame(MatchGameJpaEntity game);
 /** Participações do jogador em partidas na fase informada, da mais recente para a mais antiga. */
 @Query("select p from MatchPlayerJpaEntity p join fetch p.game g left join fetch g.winner where p.user = :user and g.phase = :phase order by g.updatedAt desc")
 List<MatchPlayerJpaEntity> findByUserAndPhase(@Param("user") UserJpaEntity user,@Param("phase") GamePhase phase);}
