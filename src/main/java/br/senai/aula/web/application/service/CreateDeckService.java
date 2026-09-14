package br.senai.aula.web.application.service;

import br.senai.aula.web.application.port.in.CreateDeckUseCase;
import br.senai.aula.web.application.port.out.DeckRepositoryPort;
import br.senai.aula.web.domain.game.Deck;

public class CreateDeckService implements CreateDeckUseCase {

    private final DeckRepositoryPort deckRepositoryPort;

    public CreateDeckService(
            DeckRepositoryPort deckRepositoryPort
    ) {
        this.deckRepositoryPort = deckRepositoryPort;
    }

    @Override
    public Deck create() {

        Deck deck = new Deck(null);

        return deckRepositoryPort.save(deck);
    }
}