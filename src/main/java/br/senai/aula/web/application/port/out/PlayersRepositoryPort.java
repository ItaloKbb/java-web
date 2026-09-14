package br.senai.aula.web.application.port.out;

import br.senai.aula.web.domain.game.Players;

import java.util.List;

public interface PlayersRepositoryPort {

    Players add(Long gameId, Long userId);

    List<Players> findByGameId(Long gameId);

    long countByGameId(Long gameId);

    boolean existsByGameIdAndUserId(Long gameId, Long userId);
}