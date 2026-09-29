package br.senai.aula.web.infrastructure.web.game.controller;
import br.senai.aula.web.application.auth.AuthenticationService;
import br.senai.aula.web.application.match.GameEngineService;
import br.senai.aula.web.infrastructure.web.game.response.GameStateResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/games")
public class GameController {
 private final AuthenticationService authentication; private final GameEngineService engine;
 public GameController(AuthenticationService authentication,GameEngineService engine){this.authentication=authentication;this.engine=engine;}
 public record CreateGameRequest(@NotBlank @Size(max=60) String name,@Min(2) @Max(6) int maxPlayers,@Min(1) @Max(10) int initialCards,@PositiveOrZero int roundReward,@PositiveOrZero int emptyHandReward,@Positive int trophyPrice){}
 public record AccessGameRequest(@NotBlank @Size(min=6,max=6) String code){}
 public record PlayRequest(@NotNull Long handCardId){}
 public record PuzzleAnswerRequest(@NotNull Long challengeId,@NotNull @Min(0) Integer alternativeIndex){}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public GameStateResponse create(@RequestHeader("X-Player-Token") String token,@Valid @RequestBody CreateGameRequest r){return engine.create(authentication.requireUser(token),r.name(),r.maxPlayers(),r.initialCards(),r.roundReward(),r.emptyHandReward(),r.trophyPrice());}
 @PostMapping("/access") public GameStateResponse access(@RequestHeader("X-Player-Token") String token,@Valid @RequestBody AccessGameRequest r){return engine.access(authentication.requireUser(token),r.code());}
 @PostMapping("/{id}/start") public GameStateResponse start(@RequestHeader("X-Player-Token") String token,@PathVariable long id){return engine.start(authentication.requireUser(token),id);}
 @GetMapping("/{id}/state") public GameStateResponse state(@RequestHeader("X-Player-Token") String token,@PathVariable long id){return engine.getState(authentication.requireUser(token),id);}
 @PostMapping("/{id}/plays") public GameStateResponse play(@RequestHeader("X-Player-Token") String token,@PathVariable long id,@Valid @RequestBody PlayRequest r){return engine.play(authentication.requireUser(token),id,r.handCardId());}
 @PostMapping("/{id}/puzzle-answers") public GameStateResponse answer(@RequestHeader("X-Player-Token") String token,@PathVariable long id,@Valid @RequestBody PuzzleAnswerRequest r){return engine.answerPuzzle(authentication.requireUser(token),id,r.challengeId(),r.alternativeIndex());}
 @PostMapping("/{id}/trophies") public GameStateResponse trophy(@RequestHeader("X-Player-Token") String token,@PathVariable long id){return engine.buyTrophy(authentication.requireUser(token),id);}
 @PostMapping("/{id}/ready") public GameStateResponse ready(@RequestHeader("X-Player-Token") String token,@PathVariable long id){return engine.ready(authentication.requireUser(token),id);}
 @PostMapping("/{id}/cancel") public GameStateResponse cancel(@RequestHeader("X-Player-Token") String token,@PathVariable long id){return engine.cancel(authentication.requireUser(token),id);}
}
