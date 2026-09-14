package br.senai.aula.web.infrastructure.web.game.controller;

import br.senai.aula.web.application.port.in.AddPlayerToGameUseCase;
import br.senai.aula.web.application.port.in.CreateDeckUseCase;
import br.senai.aula.web.application.port.in.GetGamePlayersUseCase;
import br.senai.aula.web.application.port.in.GetGameUseCase;
import br.senai.aula.web.application.port.in.GetRoundUseCase;
import br.senai.aula.web.application.port.in.StartGameUseCase;
import br.senai.aula.web.application.port.in.StartRoundUseCase;

import br.senai.aula.web.domain.game.Deck;
import br.senai.aula.web.domain.game.Game;
import br.senai.aula.web.domain.game.Players;
import br.senai.aula.web.domain.game.Round;

import br.senai.aula.web.infrastructure.web.game.request.AddPlayerRequest;
import br.senai.aula.web.infrastructure.web.game.request.StartGameRequest;

import br.senai.aula.web.infrastructure.web.game.response.DeckResponse;
import br.senai.aula.web.infrastructure.web.game.response.GameResponse;
import br.senai.aula.web.infrastructure.web.game.response.PlayerResponse;
import br.senai.aula.web.infrastructure.web.game.response.RoundResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/games")
public class GameController {

    private final StartGameUseCase startGameUseCase;
    private final GetGameUseCase getGameUseCase;
    private final AddPlayerToGameUseCase addPlayerToGameUseCase;
    private final GetGamePlayersUseCase getGamePlayersUseCase;
    private final StartRoundUseCase startRoundUseCase;
    private final GetRoundUseCase getRoundUseCase;
    private final CreateDeckUseCase createDeckUseCase;

    public GameController(
            StartGameUseCase startGameUseCase,
            GetGameUseCase getGameUseCase,
            AddPlayerToGameUseCase addPlayerToGameUseCase,
            GetGamePlayersUseCase getGamePlayersUseCase,
            StartRoundUseCase startRoundUseCase,
            GetRoundUseCase getRoundUseCase,
            CreateDeckUseCase createDeckUseCase
    ) {
        this.startGameUseCase = startGameUseCase;
        this.getGameUseCase = getGameUseCase;
        this.addPlayerToGameUseCase = addPlayerToGameUseCase;
        this.getGamePlayersUseCase = getGamePlayersUseCase;
        this.startRoundUseCase = startRoundUseCase;
        this.getRoundUseCase = getRoundUseCase;
        this.createDeckUseCase = createDeckUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameResponse start(
            @Valid @RequestBody StartGameRequest request
    ) {

        Game game = startGameUseCase.start(
                request.name(),
                request.maxPlayers()
        );

        return GameResponse.from(game);
    }

    @GetMapping("/{gameId}")
    public GameResponse getGame(
            @PathVariable Long gameId
    ) {

        Game game = getGameUseCase.getById(gameId);

        return GameResponse.from(game);
    }

    @PostMapping("/{gameId}/players")
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerResponse addPlayer(
            @PathVariable Long gameId,
            @Valid @RequestBody AddPlayerRequest request
    ) {

        Players player = addPlayerToGameUseCase.addPlayer(
                gameId,
                request.userId()
        );

        return PlayerResponse.from(player);
    }

    @GetMapping("/{gameId}/players")
    public List<PlayerResponse> getPlayers(
            @PathVariable Long gameId
    ) {

        return getGamePlayersUseCase
                .getPlayers(gameId)
                .stream()
                .map(PlayerResponse::from)
                .toList();
    }

    @PostMapping("/{gameId}/rounds")
    @ResponseStatus(HttpStatus.CREATED)
    public RoundResponse startRound(
            @PathVariable Long gameId
    ) {

        Round round = startRoundUseCase.startRound(gameId);

        return RoundResponse.from(round);
    }

    @GetMapping("/rounds/{roundId}")
    public RoundResponse getRound(
            @PathVariable Long roundId
    ) {

        Round round = getRoundUseCase.getById(roundId);

        return RoundResponse.from(round);
    }

    @PostMapping("/deck")
    @ResponseStatus(HttpStatus.CREATED)
    public DeckResponse createDeck() {

        Deck deck = createDeckUseCase.create();

        return DeckResponse.from(deck);
    }
}