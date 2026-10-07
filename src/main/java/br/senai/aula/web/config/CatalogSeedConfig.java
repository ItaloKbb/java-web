package br.senai.aula.web.config;

import br.senai.aula.web.domain.cards.Naipe;
import br.senai.aula.web.domain.cards.Valor;
import br.senai.aula.web.domain.skills.SkillType;
import br.senai.aula.web.infrastructure.persistence.card.entity.entity.CardEntity;
import br.senai.aula.web.infrastructure.persistence.card.entity.repository.CardJpaRepository;
import br.senai.aula.web.infrastructure.persistence.puzzle.entity.PuzzleJpaEntity;
import br.senai.aula.web.infrastructure.persistence.puzzle.repository.PuzzleJpaRepository;
import br.senai.aula.web.infrastructure.persistence.skills.entity.SkillsJpaEntity;
import br.senai.aula.web.infrastructure.persistence.skills.repository.SkillsJpaRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogSeedConfig {
    @Bean
    ApplicationRunner catalogSeed(CardJpaRepository cards, SkillsJpaRepository skills, PuzzleJpaRepository puzzles) {
        return args -> {
            for (Valor valor : Valor.values()) for (Naipe naipe : Naipe.values())
                cards.findByValorAndNaipe(valor, naipe).orElseGet(() -> cards.save(new CardEntity(null, valor, naipe, null)));
            if (skills.count() == 0) {
            seedSkill(skills,"Bloqueio","O próximo jogador perde o turno",SkillType.BLOCK,Naipe.PAUS,Valor.QUATRO);
            seedSkill(skills,"Inversão","Inverte o sentido da rodada",SkillType.INVERTS,Naipe.ESPADAS,Valor.SEIS);
            seedSkill(skills,"Queima","Descarta outra carta aleatória da própria mão",SkillType.BURN,Naipe.PAUS,Valor.DAMA);
            seedSkill(skills,"Troca de mãos","Troca a mão com o próximo jogador",SkillType.CHANGEOFHANDS,Naipe.OUROS,Valor.AS);
            seedSkill(skills,"Bomba","Todos os adversários compram uma carta",SkillType.BOMB,Naipe.PAUS,Valor.DOIS);
            seedSkill(skills,"Escudo","Bloqueia o próximo efeito negativo",SkillType.SHIELD,Naipe.COPAS,Valor.TRES);
            }
            String surpriseDescription="Ganha ou perde de 1 a 4 moedas conforme o naipe";
            for (Naipe naipe : Naipe.values())
                seedSkill(skills,"Surpresa",surpriseDescription,SkillType.SURPRISE,naipe,Valor.VALETE);
            String buyDescription="O próximo jogador compra 2, 3 ou 4 cartas conforme o naipe";
            for (Naipe naipe : Naipe.values())
                seedSkill(skills,"Compra",buyDescription,SkillType.BUY,naipe,Valor.SETE);
            String puzzleDescription="Acerto ganha e erro compra de 1 a 4 conforme o naipe";
            for (Naipe naipe : Naipe.values())
                seedSkill(skills,"Puzzle",puzzleDescription,SkillType.PUZZLE,naipe,Valor.REI);
            skills.findByValorAndNaipe(Valor.REI,Naipe.ESPADAS).ifPresent(skill -> {
                if (skill.getType()==SkillType.PUZZLE && "Acerto recebe duas moedas; erro compra duas cartas".equals(skill.getDescription())) {
                    skill.update(skill.getName(),puzzleDescription,skill.getType(),skill.getNaipe(),skill.getValor());
                    skills.save(skill);
                }
            });
            String theftDescription="Rouba cartas ou moedas do próximo jogador conforme o naipe";
            for (Naipe naipe : Naipe.values())
                seedSkill(skills,"Roubo",theftDescription,SkillType.THEFT,naipe,Valor.CINCO);
            skills.findByValorAndNaipe(Valor.CINCO,Naipe.COPAS).ifPresent(skill -> {
                if (skill.getType()==SkillType.THEFT && "Rouba uma carta do próximo jogador".equals(skill.getDescription())) {
                    skill.update(skill.getName(),theftDescription,skill.getType(),skill.getNaipe(),skill.getValor());
                    skills.save(skill);
                }
            });
            skills.findByValorAndNaipe(Valor.SETE,Naipe.OUROS).ifPresent(skill -> {
                if (skill.getType()==SkillType.BUY && "O próximo jogador compra duas cartas".equals(skill.getDescription())) {
                    skill.update(skill.getName(),buyDescription,skill.getType(),skill.getNaipe(),skill.getValor());
                    skills.save(skill);
                }
            });
            skills.findByValorAndNaipe(Valor.VALETE,Naipe.COPAS).ifPresent(skill -> {
                if (skill.getType()==SkillType.SURPRISE && "Recebe uma moeda".equals(skill.getDescription())) {
                    skill.update(skill.getName(),surpriseDescription,skill.getType(),skill.getNaipe(),skill.getValor());
                    skills.save(skill);
                }
            });
            if (puzzles.count() == 0) {
                puzzles.save(new PuzzleJpaEntity("Quanto é 7 x 8?", new String[]{"54","56","64","48"}, 1));
                puzzles.save(new PuzzleJpaEntity("Qual estrutura repete um bloco enquanto uma condição for verdadeira?", new String[]{"if","while","class","import"}, 1));
                puzzles.save(new PuzzleJpaEntity("Qual protocolo é usado normalmente por APIs REST?", new String[]{"FTP","HTTP","SMTP","SSH"}, 1));
            }
        };
    }
    private void seedSkill(SkillsJpaRepository repository,String name,String description,SkillType type,Naipe suit,Valor value){
        repository.findByValorAndNaipe(value,suit).orElseGet(() -> repository.save(new SkillsJpaEntity(null,name,description,type,suit,value)));
    }
}
