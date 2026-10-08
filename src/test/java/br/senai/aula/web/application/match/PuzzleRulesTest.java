package br.senai.aula.web.application.match;

import br.senai.aula.web.domain.cards.Naipe;
import br.senai.aula.web.infrastructure.persistence.puzzle.entity.PuzzleJpaEntity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PuzzleRulesTest {
    @Test
    void rewardAndPenaltyFollowSuitOrder() {
        assertEquals(1, GameEngineService.puzzlePower(Naipe.OUROS));
        assertEquals(2, GameEngineService.puzzlePower(Naipe.ESPADAS));
        assertEquals(3, GameEngineService.puzzlePower(Naipe.COPAS));
        assertEquals(4, GameEngineService.puzzlePower(Naipe.PAUS));
    }

    @Test
    void usesEachActivePuzzleBeforeStartingAnotherCycle() {
        List<PuzzleJpaEntity> active = List.of(
            new PuzzleJpaEntity(1L, new String[]{"A", "B"}, 0),
            new PuzzleJpaEntity(2L, new String[]{"A", "B"}, 0),
            new PuzzleJpaEntity(3L, new String[]{"A", "B"}, 0)
        );
        List<Long> history = new ArrayList<>();
        Random random = new Random(5);

        for (int cycle = 0; cycle < 2; cycle++) {
            Set<Long> seenThisCycle = new HashSet<>();
            for (int i = 0; i < active.size(); i++) {
                PuzzleJpaEntity selected = GameEngineService.selectPuzzle(active, history, random);
                assertFalse(seenThisCycle.contains(selected.getId()));
                seenThisCycle.add(selected.getId());
                history.add(selected.getId());
            }
            assertEquals(active.size(), seenThisCycle.size());
        }
        assertEquals(6, history.size());
    }

    @Test
    void requiresAtLeastOneActivePuzzle() {
        assertThrows(IllegalStateException.class,
            () -> GameEngineService.selectPuzzle(List.of(), List.of(), new Random(5)));
    }
}
