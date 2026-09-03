package br.senai.aula.web.application.port.out;

import br.senai.aula.web.domain.skills.Skills;

import java.util.List;
import java.util.Optional;

public interface SkillsRepositoryPort {

    Skills save(Skills skill);

    Optional<Skills> findById(Long id);

    List<Skills> findAll();

    void deleteById(Long id);
}