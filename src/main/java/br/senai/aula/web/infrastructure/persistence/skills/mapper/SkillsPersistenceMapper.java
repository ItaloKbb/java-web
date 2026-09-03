package br.senai.aula.web.infrastructure.persistence.skills.mapper;

import br.senai.aula.web.domain.skills.Skills;
import br.senai.aula.web.infrastructure.persistence.skills.entity.SkillsJpaEntity;

public final class SkillsPersistenceMapper {

    private SkillsPersistenceMapper() {
    }

    public static SkillsJpaEntity toEntity(Skills skill) {
        return new SkillsJpaEntity(
                skill.id(),
                skill.name(),
                skill.description(),
                skill.type(),
                skill.naipe()
        );
    }

    public static Skills toDomain(SkillsJpaEntity entity) {
        return new Skills(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getType(),
                entity.getNaipe()
        );
    }
}