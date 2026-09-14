package br.senai.aula.web.application.service;

import br.senai.aula.web.application.port.in.GetGamePlayersUseCase;
import br.senai.aula.web.application.port.out.PlayersRepositoryPort;
import br.senai.aula.web.domain.game.Players;

import java.util.List;

public class GetGamePlayersService implements GetGamePlayersUseCase {

    private final PlayersRepositoryPort playersRepositoryPort;

    public GetGamePlayersService(
            PlayersRepositoryPort playersRepositoryPort
    ) {
        this.playersRepositoryPort = playersRepositoryPort;
    }

    @Override
    public List<Players> getPlayers(Long gameId) {
        return playersRepositoryPort.findByGameId(gameId);
    }
}