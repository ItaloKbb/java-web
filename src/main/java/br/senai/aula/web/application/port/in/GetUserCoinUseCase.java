package br.senai.aula.web.application.port.in;

import br.senai.aula.web.domain.user.Coin;

public interface GetUserCoinUseCase {

    Coin getByUserId(Long userId);
}
