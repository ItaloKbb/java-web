package br.senai.aula.web.infrastructure.web.skill.request;

import br.senai.aula.web.domain.cards.Naipe;
import br.senai.aula.web.domain.cards.Valor;
import br.senai.aula.web.domain.skills.SkillType;
import jakarta.validation.constraints.NotBlank;

public record CreateSkillRequest (
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Description is required") String description,
        @NotBlank(message = "Type is required") SkillType type,
        @NotBlank(message = "Naipe is required") Naipe naipe,
        @NotBlank(message = "Value is required") Valor valor

){

}
