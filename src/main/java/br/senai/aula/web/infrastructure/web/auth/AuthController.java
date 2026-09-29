package br.senai.aula.web.infrastructure.web.auth;

import br.senai.aula.web.application.auth.AuthenticationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/sessions")
public class AuthController {
    private final AuthenticationService authentication;
    public AuthController(AuthenticationService authentication) { this.authentication = authentication; }

    public record LoginRequest(@NotBlank @Size(max = 30) String nickname, @NotBlank @Size(min = 4, max = 30) String code) {}

    @PostMapping
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authentication.login(request.nickname(), request.code());
    }
}
