package br.senai.aula.web.infrastructure.web.profile;

import br.senai.aula.web.application.auth.AuthenticationService;
import br.senai.aula.web.application.profile.ProfileService;
import br.senai.aula.web.application.profile.ProfileService.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Perfil do jogador autenticado (`X-Player-Token`), consumido pela tela `/perfil` do app. */
@RestController @RequestMapping("/profile")
public class ProfileController {
    private final AuthenticationService authentication;
    private final ProfileService profile;

    public ProfileController(AuthenticationService authentication, ProfileService profile) {
        this.authentication = authentication;
        this.profile = profile;
    }

    @GetMapping public ProfileResponse get(@RequestHeader("X-Player-Token") String token) { return profile.profile(authentication.requireUser(token)); }
    @GetMapping("/stats") public StatsResponse stats(@RequestHeader("X-Player-Token") String token) { return profile.stats(authentication.requireUser(token)); }
    @GetMapping("/collection") public List<CollectionCardResponse> collection(@RequestHeader("X-Player-Token") String token) { authentication.requireUser(token); return profile.collection(); }
    @GetMapping("/achievements") public List<AchievementResponse> achievements(@RequestHeader("X-Player-Token") String token) { return profile.achievements(authentication.requireUser(token)); }
    @GetMapping("/activities") public List<ActivityResponse> activities(@RequestHeader("X-Player-Token") String token) { return profile.activities(authentication.requireUser(token)); }
}
