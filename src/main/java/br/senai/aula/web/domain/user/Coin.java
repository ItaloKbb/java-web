package br.senai.aula.web.domain.user;

import java.math.BigDecimal;

public record Coin(Long id, BigDecimal balance) {

    public static Coin empty() {
        return new Coin(null, BigDecimal.ZERO.setScale(2));
    }
}
