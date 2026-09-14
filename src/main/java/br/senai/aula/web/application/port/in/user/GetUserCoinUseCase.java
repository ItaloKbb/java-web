package br.senai.aula.web.application.port.in.user;

import br.senai.aula.web.domain.user.Coin;

public interface GetUserCoinUseCase {

    Coin getByUserId(Long userId);
}
