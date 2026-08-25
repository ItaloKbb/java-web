package br.senai.aula.web.domain.skills;

enum SkillType {

    BLOCK, THEFT, INVERTS, BUY, BURN, SURPRISE, PUZZLE, CHANGEOFHANDS, BOMB, SHIELD

}

public record Skills(Long id, String name, String description, SkillType type) {

    public static Skills newSkill(String name, String description, SkillType type) {
        return new Skills(null, name, description, type);
    }


}
