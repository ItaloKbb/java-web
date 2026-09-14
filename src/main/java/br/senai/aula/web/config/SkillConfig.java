package br.senai.aula.web.config;

import br.senai.aula.web.application.port.in.skill.CreateSkillUseCase;
import br.senai.aula.web.application.port.in.skill.DeleteSkillUseCase;
import br.senai.aula.web.application.port.out.SkillsRepositoryPort;
import br.senai.aula.web.application.service.CreateSkillService;
import br.senai.aula.web.application.service.DeleteSkillService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SkillConfig {
    @Bean
    public CreateSkillUseCase createSkillUseCase(SkillsRepositoryPort skillsRepository) {
        return new CreateSkillService(skillsRepository);
    }
    @Bean
    public DeleteSkillUseCase deleteSkillUseCase(SkillsRepositoryPort skillsRepository) {
        return new DeleteSkillService(skillsRepository);

    }


}
