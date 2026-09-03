package br.senai.aula.web.infrastructure.persistence.skills.repository;

import br.senai.aula.web.infrastructure.persistence.skills.entity.SkillsJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillsJpaRepository extends JpaRepository<SkillsJpaEntity, Long> {
}