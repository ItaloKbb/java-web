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
            seedSkill(skills,"Bloqueio","O próximo jogador perde o turno",SkillType.BLOCK,Naipe.PAUS,Valor.QUATRO);
            seedSkill(skills,"Roubo","Rouba uma carta do próximo jogador",SkillType.THEFT,Naipe.COPAS,Valor.CINCO);
            seedSkill(skills,"Inversão","Inverte o sentido da rodada",SkillType.INVERTS,Naipe.ESPADAS,Valor.SEIS);
            seedSkill(skills,"Compra","O próximo jogador compra duas cartas",SkillType.BUY,Naipe.OUROS,Valor.SETE);
            seedSkill(skills,"Queima","Descarta outra carta aleatória da própria mão",SkillType.BURN,Naipe.PAUS,Valor.DAMA);
            seedSkill(skills,"Surpresa","Recebe uma moeda",SkillType.SURPRISE,Naipe.COPAS,Valor.VALETE);
            seedSkill(skills,"Puzzle","Acerto recebe duas moedas; erro compra duas cartas",SkillType.PUZZLE,Naipe.ESPADAS,Valor.REI);
            seedSkill(skills,"Troca de mãos","Troca a mão com o próximo jogador",SkillType.CHANGEOFHANDS,Naipe.OUROS,Valor.AS);
            seedSkill(skills,"Bomba","Todos os adversários compram uma carta",SkillType.BOMB,Naipe.PAUS,Valor.DOIS);
            seedSkill(skills,"Escudo","Bloqueia o próximo efeito negativo",SkillType.SHIELD,Naipe.COPAS,Valor.TRES);
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
