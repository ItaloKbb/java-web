package br.senai.aula.web.infrastructure.web.admin;

import br.senai.aula.web.application.auth.UnauthorizedException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminSessionService {
    private static final Duration SESSION_LENGTH = Duration.ofHours(8);
    private final ConcurrentHashMap<String, Instant> sessions = new ConcurrentHashMap<>();

    public String login(String username, String password) {
        if (!matches(username, "aluno") || !matches(password, "aluno")) {
            throw new UnauthorizedException("Usuário ou senha incorretos");
        }
        String token = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, Instant.now().plus(SESSION_LENGTH));
        return token;
    }

    public void require(String token) {
        Instant expiresAt = token == null ? null : sessions.get(token);
        if (expiresAt == null || !expiresAt.isAfter(Instant.now())) {
            if (token != null) sessions.remove(token);
            throw new UnauthorizedException("Sessão administrativa inválida ou expirada");
        }
    }

    public void logout(String token) {
        require(token);
        sessions.remove(token);
    }

    private boolean matches(String value, String expected) {
        return value != null && MessageDigest.isEqual(
                value.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
    }
}
