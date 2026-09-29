package br.senai.aula.web.infrastructure.web.skill.request;

import br.senai.aula.web.domain.cards.Naipe;
import br.senai.aula.web.domain.cards.Valor;
import br.senai.aula.web.domain.skills.SkillType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSkillRequest (
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Description is required") String description,
        @NotNull(message = "Type is required") SkillType type,
        @NotNull(message = "Naipe is required") Naipe naipe,
        @NotNull(message = "Value is required") Valor valor

){

}
