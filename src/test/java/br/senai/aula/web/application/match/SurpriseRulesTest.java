package br.senai.aula.web.application.match;

import br.senai.aula.web.domain.cards.Naipe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SurpriseRulesTest {
    @Test
    void powerFollowsSuitOrder() {
        assertEquals(1, GameEngineService.surprisePower(Naipe.OUROS));
        assertEquals(2, GameEngineService.surprisePower(Naipe.ESPADAS));
        assertEquals(3, GameEngineService.surprisePower(Naipe.COPAS));
        assertEquals(4, GameEngineService.surprisePower(Naipe.PAUS));
    }

    @Test
    void badSurpriseCannotMakeCoinsNegative() {
        assertEquals(-2, GameEngineService.surpriseCoinDelta(2, -4));
        assertEquals(0, GameEngineService.surpriseCoinDelta(0, -3));
        assertEquals(4, GameEngineService.surpriseCoinDelta(2, 4));
    }
}
