package br.senai.aula.web.application.service;

import br.senai.aula.web.application.port.in.skill.DeleteSkillUseCase;
import br.senai.aula.web.application.port.out.SkillsRepositoryPort;

import java.util.NoSuchElementException;

public class DeleteSkillService implements DeleteSkillUseCase {

    private final SkillsRepositoryPort skillsRepository;

    public DeleteSkillService(SkillsRepositoryPort skillsRepository) {this.skillsRepository = skillsRepository;}

    @Override
    public void delete(Long id) {
        skillsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Skill não encontrada: " + id));
        skillsRepository.deleteById(id);
    }
}
