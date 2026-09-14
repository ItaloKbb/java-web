package br.senai.aula.web.config;

import br.senai.aula.web.application.port.in.AddPlayerToGameUseCase;
import br.senai.aula.web.application.port.in.CreateDeckUseCase;
import br.senai.aula.web.application.port.in.GetGamePlayersUseCase;
import br.senai.aula.web.application.port.in.GetGameUseCase;
import br.senai.aula.web.application.port.in.GetRoundUseCase;
import br.senai.aula.web.application.port.in.StartGameUseCase;
import br.senai.aula.web.application.port.in.StartRoundUseCase;

import br.senai.aula.web.application.port.out.DeckRepositoryPort;
import br.senai.aula.web.application.port.out.GameRepositoryPort;
import br.senai.aula.web.application.port.out.PlayersRepositoryPort;
import br.senai.aula.web.application.port.out.RoundRepositoryPort;

import br.senai.aula.web.application.service.AddPlayerToGameService;
import br.senai.aula.web.application.service.CreateDeckService;
import br.senai.aula.web.application.service.GetGamePlayersService;
import br.senai.aula.web.application.service.GetGameService;
import br.senai.aula.web.application.service.GetRoundService;
import br.senai.aula.web.application.service.StartGameService;
import br.senai.aula.web.application.service.StartRoundService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GameConfig {

    @Bean
    StartGameUseCase startGameUseCase(
            GameRepositoryPort gameRepositoryPort
    ) {
        return new StartGameService(gameRepositoryPort);
    }

    @Bean
    GetGameUseCase getGameUseCase(
            GameRepositoryPort gameRepositoryPort
    ) {
        return new GetGameService(gameRepositoryPort);
    }

    @Bean
    AddPlayerToGameUseCase addPlayerToGameUseCase(
            GameRepositoryPort gameRepositoryPort,
            PlayersRepositoryPort playersRepositoryPort
    ) {
        return new AddPlayerToGameService(
                gameRepositoryPort,
                playersRepositoryPort
        );
    }

    @Bean
    GetGamePlayersUseCase getGamePlayersUseCase(
            PlayersRepositoryPort playersRepositoryPort
    ) {
        return new GetGamePlayersService(
                playersRepositoryPort
        );
    }

    @Bean
    StartRoundUseCase startRoundUseCase(
            GameRepositoryPort gameRepositoryPort,
            RoundRepositoryPort roundRepositoryPort
    ) {
        return new StartRoundService(
                gameRepositoryPort,
                roundRepositoryPort
        );
    }

    @Bean
    GetRoundUseCase getRoundUseCase(
            RoundRepositoryPort roundRepositoryPort
    ) {
        return new GetRoundService(
                roundRepositoryPort
        );
    }

    @Bean
    CreateDeckUseCase createDeckUseCase(
            DeckRepositoryPort deckRepositoryPort
    ) {
        return new CreateDeckService(
                deckRepositoryPort
        );
    }
}