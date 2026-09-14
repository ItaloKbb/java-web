package br.senai.aula.web.application.port.out;

import br.senai.aula.web.domain.game.Game;

import java.util.Optional;

public interface GameRepositoryPort {

    Game save(Game game);

    Optional<Game> findById(Long id);
}