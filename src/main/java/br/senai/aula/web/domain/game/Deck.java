package br.senai.aula.web.domain.game;

import javax.smartcardio.Card;
import java.util.List;

public record Deck(List<Card> cards) {

    public static Deck empty() {
        return new Deck(List.of());
    }
}
