package br.senai.aula.web.application.service;

import br.senai.aula.web.application.port.in.skill.CreateSkillUseCase;
import br.senai.aula.web.application.port.out.SkillsRepositoryPort;
import br.senai.aula.web.domain.cards.Naipe;
import br.senai.aula.web.domain.cards.Valor;
import br.senai.aula.web.domain.skills.SkillType;
import br.senai.aula.web.domain.skills.Skills;

public class CreateSkillService implements CreateSkillUseCase {

    private final SkillsRepositoryPort skillsRepository;

    public CreateSkillService(SkillsRepositoryPort skillsRepository) {this.skillsRepository = skillsRepository;}

    @Override
    public Skills create (String name, String description, SkillType type, Naipe naipe, Valor valor) {
        return skillsRepository.save(Skills.newSkill(name, description, type, naipe, valor));
    }



    }

