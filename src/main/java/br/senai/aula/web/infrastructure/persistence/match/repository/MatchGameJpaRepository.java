package br.senai.aula.web.infrastructure.persistence.match.repository;
import br.senai.aula.web.infrastructure.persistence.match.entity.MatchGameJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface MatchGameJpaRepository extends JpaRepository<MatchGameJpaEntity,Long>{Optional<MatchGameJpaEntity> findByAccessCodeIgnoreCase(String code); boolean existsByAccessCode(String code);}
