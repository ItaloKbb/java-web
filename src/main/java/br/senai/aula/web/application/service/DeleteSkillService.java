package br.senai.aula.web.application.service;

import br.senai.aula.web.application.port.in.skill.DeleteSkillUseCase;
import br.senai.aula.web.application.port.out.SkillsRepositoryPort;
import br.senai.aula.web.domain.skills.Skills;

public class DeleteSkillService implements DeleteSkillUseCase {

    private final SkillsRepositoryPort skillsRepository;

    public DeleteSkillService(SkillsRepositoryPort skillsRepository) {this.skillsRepository = skillsRepository;}

    @Override
    public void delete(Long id) {

    }
}
