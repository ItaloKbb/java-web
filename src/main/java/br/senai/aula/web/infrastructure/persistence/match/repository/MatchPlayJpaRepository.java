package br.senai.aula.web.infrastructure.persistence.match.repository;
import br.senai.aula.web.infrastructure.persistence.match.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MatchPlayJpaRepository extends JpaRepository<MatchPlayJpaEntity,Long>{List<MatchPlayJpaEntity> findByRoundOrderByPlayOrder(MatchRoundJpaEntity round);boolean existsByRoundAndPlayer(MatchRoundJpaEntity r,MatchPlayerJpaEntity p);}
