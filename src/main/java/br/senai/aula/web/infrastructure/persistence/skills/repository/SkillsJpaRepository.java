package br.senai.aula.web.infrastructure.persistence.skills.repository;

import br.senai.aula.web.infrastructure.persistence.skills.entity.SkillsJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import br.senai.aula.web.domain.cards.Naipe;
import br.senai.aula.web.domain.cards.Valor;
import java.util.Optional;

public interface SkillsJpaRepository extends JpaRepository<SkillsJpaEntity, Long> {
    Optional<SkillsJpaEntity> findByValorAndNaipe(Valor valor, Naipe naipe);
}
