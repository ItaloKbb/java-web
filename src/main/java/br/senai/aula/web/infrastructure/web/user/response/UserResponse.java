package br.senai.aula.web.infrastructure.web.user.response;

import br.senai.aula.web.domain.user.User;

public record UserResponse(Long id, String name, String email, CoinResponse coin) {

    public static UserResponse from(User user) {
        return new UserResponse(user.id(), user.name(), user.email(), CoinResponse.from(user.coin()));
    }
}
