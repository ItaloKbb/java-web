package br.senai.aula.web.infrastructure.web.skill.controller;
import br.senai.aula.web.infrastructure.persistence.skills.repository.SkillsJpaRepository;
import br.senai.aula.web.infrastructure.web.skill.response.SkillResponse;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/skills") public class SkillController {private final SkillsJpaRepository skills;public SkillController(SkillsJpaRepository skills){this.skills=skills;}@GetMapping public List<SkillResponse> list(){return skills.findAll().stream().map(s->new SkillResponse(s.getId(),s.getName(),s.getDescription(),s.getType(),s.getNaipe(),s.getValor())).toList();}}
