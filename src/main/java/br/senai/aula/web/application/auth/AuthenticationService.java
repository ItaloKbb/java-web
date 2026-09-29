package br.senai.aula.web.application.auth;

import br.senai.aula.web.infrastructure.persistence.user.entity.UserJpaEntity;
import br.senai.aula.web.infrastructure.persistence.user.entity.UserSessionJpaEntity;
import br.senai.aula.web.infrastructure.persistence.user.repository.UserJpaRepository;
import br.senai.aula.web.infrastructure.persistence.user.repository.UserSessionJpaRepository;
import br.senai.aula.web.infrastructure.web.auth.AuthResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthenticationService {
    private final UserJpaRepository users;
    private final UserSessionJpaRepository sessions;
    private final SecureRandom random = new SecureRandom();

    public AuthenticationService(UserJpaRepository users, UserSessionJpaRepository sessions) {
        this.users = users;
        this.sessions = sessions;
    }

    @Transactional
    public AuthResponse login(String nickname, String code) {
        String cleanNickname = nickname.trim();
        UserJpaEntity user = users.findByNicknameIgnoreCase(cleanNickname).orElseGet(() -> {
            String salt = randomHex(16);
            return users.save(new UserJpaEntity(cleanNickname, hash(code, salt), salt));
        });
        if (!MessageDigest.isEqual(user.getCodeHash().getBytes(StandardCharsets.UTF_8),
                hash(code, user.getCodeSalt()).getBytes(StandardCharsets.UTF_8))) {
            throw new UnauthorizedException("Nickname ou code incorreto");
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        UserSessionJpaEntity session = sessions.findByUser(user).orElseGet(() -> new UserSessionJpaEntity(token, user));
        session.replaceToken(token);
        sessions.save(session);
        return AuthResponse.from(token, user);
    }

    @Transactional(readOnly = true)
    public UserJpaEntity requireUser(String token) {
        if (token == null || token.isBlank()) throw new UnauthorizedException("Token não informado");
        return sessions.findByToken(token).map(UserSessionJpaEntity::getUser)
                .orElseThrow(() -> new UnauthorizedException("Token inválido"));
    }

    private String randomHex(int bytes) { byte[] value = new byte[bytes]; random.nextBytes(value); return HexFormat.of().formatHex(value); }
    private String hash(String code, String salt) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest((salt + ":" + code).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) { throw new IllegalStateException("SHA-256 indisponível"); }
    }
}
