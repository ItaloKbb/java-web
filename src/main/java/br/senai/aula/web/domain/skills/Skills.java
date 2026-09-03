package br.senai.aula.web.domain.skills;

import br.senai.aula.web.domain.cards.Naipe;

public record Skills(Long id, String name, String description, SkillType type, Naipe naipe) {

    public static Skills newSkill(String name, String description, SkillType type, Naipe naipe) {
        return new Skills(null, name, description, type, naipe);
    }

}
