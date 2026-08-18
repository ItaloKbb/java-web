package br.senai.aula.web.domain.user;

public record User(Long id, String name, String email, Coin coin) {

    public static User newUser(String name, String email) {
        return new User(null, name, email, Coin.empty());
    }
}
