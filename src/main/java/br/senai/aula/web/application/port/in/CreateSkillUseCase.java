package br.senai.aula.web.application.port.in;


import br.senai.aula.web.domain.skills.Skills;
import br.senai.aula.web.domain.skills.SkillType;
import br.senai.aula.web.domain.cards.Naipe;

public interface CreateSkillUseCase {

    Skills create(String name, String description, SkillType type, Naipe naipe);
}