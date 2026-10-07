package br.senai.aula.web.infrastructure.persistence.match.repository;
import br.senai.aula.web.domain.match.RoundStatus;
import br.senai.aula.web.infrastructure.persistence.match.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface MatchRoundJpaRepository extends JpaRepository<MatchRoundJpaEntity,Long>{Optional<MatchRoundJpaEntity> findFirstByGameOrderByNumberDesc(MatchGameJpaEntity game);List<MatchRoundJpaEntity> findByGameAndStatusOrderByNumberAsc(MatchGameJpaEntity game,RoundStatus status);}
