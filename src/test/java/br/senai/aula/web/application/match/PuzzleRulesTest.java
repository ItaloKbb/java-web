package br.senai.aula.web.application.match;

import br.senai.aula.web.domain.cards.Naipe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PuzzleRulesTest {
    @Test
    void rewardAndPenaltyFollowSuitOrder() {
        assertEquals(1, GameEngineService.puzzlePower(Naipe.OUROS));
        assertEquals(2, GameEngineService.puzzlePower(Naipe.ESPADAS));
        assertEquals(3, GameEngineService.puzzlePower(Naipe.COPAS));
        assertEquals(4, GameEngineService.puzzlePower(Naipe.PAUS));
    }
}
