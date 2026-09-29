package br.senai.aula.web.infrastructure.web.auth;

import br.senai.aula.web.infrastructure.persistence.user.entity.UserJpaEntity;

public record AuthResponse(String token, UserResponse user) {
    public record UserResponse(Long id, String nickname, Integer rankingPoints) {}
    public static AuthResponse from(String token, UserJpaEntity user) {
        return new AuthResponse(token, new UserResponse(user.getId(), user.getNickname(), user.getRankingPoints()));
    }
}
