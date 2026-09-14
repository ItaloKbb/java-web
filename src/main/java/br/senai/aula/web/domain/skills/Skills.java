package br.senai.aula.web.domain.skills;

import br.senai.aula.web.domain.cards.Naipe;
import br.senai.aula.web.domain.cards.Valor;

public record Skills(Long id, String name, String description, SkillType type, Naipe naipe, Valor valor) {

    public static Skills newSkill(String name, String description, SkillType type, Naipe naipe, Valor valor) {
        return new Skills(null, name, description, type, naipe, valor);
    }

}
