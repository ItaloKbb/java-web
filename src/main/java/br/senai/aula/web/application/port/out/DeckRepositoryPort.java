package br.senai.aula.web.application.port.out;

import br.senai.aula.web.domain.game.Deck;

import java.util.Optional;

public interface DeckRepositoryPort {

    Deck save(Deck deck);

    Optional<Deck> findById(Long id);
}