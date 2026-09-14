package br.senai.aula.web.infrastructure.web.skill.response;

import br.senai.aula.web.domain.cards.Naipe;
import br.senai.aula.web.domain.cards.Valor;
import br.senai.aula.web.domain.skills.SkillType;
import br.senai.aula.web.domain.skills.Skills;

public record SkillResponse(Long id, String name, String description, SkillType type, Naipe naipe, Valor valor) {

    public static br.senai.aula.web.infrastructure.web.skill.response.SkillResponse from(Skills skill) {
        return new br.senai.aula.web.infrastructure.web.skill.response.SkillResponse(skill.id(), skill.name(), skill.description(), skill.type(), skill.naipe(), skill.valor() );
    }
}
