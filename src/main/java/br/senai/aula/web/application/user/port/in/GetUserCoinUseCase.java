package br.senai.aula.web.application.user.port.in;

import br.senai.aula.web.domain.user.Coin;

public interface GetUserCoinUseCase {

    Coin getByUserId(Long userId);
}
