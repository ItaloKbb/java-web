package br.senai.aula.web.infrastructure.web.user.response;

import br.senai.aula.web.domain.user.Coin;

import java.math.BigDecimal;

public record CoinResponse(Long id, BigDecimal balance) {

    public static CoinResponse from(Coin coin) {
        return new CoinResponse(coin.id(), coin.balance());
    }
}
