package br.senai.aula.web.infrastructure.persistence.match.repository;
import br.senai.aula.web.domain.match.CardZone;
import br.senai.aula.web.infrastructure.persistence.match.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface MatchCardJpaRepository extends JpaRepository<MatchCardJpaEntity,Long>{List<MatchCardJpaEntity> findByGameAndZoneOrderByDrawOrder(MatchGameJpaEntity g,CardZone z);List<MatchCardJpaEntity> findByGameAndOwnerAndZone(MatchGameJpaEntity g,MatchPlayerJpaEntity p,CardZone z);Optional<MatchCardJpaEntity> findByIdAndGame(Long id,MatchGameJpaEntity g);long countByGameAndOwnerAndZone(MatchGameJpaEntity g,MatchPlayerJpaEntity p,CardZone z);}
