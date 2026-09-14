package br.senai.aula.web.application.port.out;

import br.senai.aula.web.domain.game.Round;

import java.util.Optional;

public interface RoundRepositoryPort {

    Round save(Round round, Long gameId);

    Optional<Round> findById(Long id);

    long countByGameId(Long gameId);
}