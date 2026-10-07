package br.senai.aula.web.application.match;

import br.senai.aula.web.domain.cards.Naipe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuyRulesTest {
    @Test
    void cardsDrawnFollowSuitPower() {
        assertEquals(2, GameEngineService.buyPower(Naipe.OUROS));
        assertEquals(2, GameEngineService.buyPower(Naipe.ESPADAS));
        assertEquals(3, GameEngineService.buyPower(Naipe.COPAS));
        assertEquals(4, GameEngineService.buyPower(Naipe.PAUS));
    }
}
