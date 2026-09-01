package br.senai.aula.web.application.service;

import br.senai.aula.web.application.port.in.GetUserCoinUseCase;
import br.senai.aula.web.application.port.out.UserRepositoryPort;
import br.senai.aula.web.domain.user.Coin;
import br.senai.aula.web.application.exception.UserNotFoundException;

public class GetUserCoinService implements GetUserCoinUseCase {

    private final UserRepositoryPort userRepository;

    public GetUserCoinService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Coin getByUserId(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId))
                .coin();
    }
}
