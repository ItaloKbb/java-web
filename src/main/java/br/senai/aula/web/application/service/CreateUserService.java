package br.senai.aula.web.application.service;

import br.senai.aula.web.application.port.in.CreateUserUseCase;
import br.senai.aula.web.application.port.out.UserRepositoryPort;
import br.senai.aula.web.domain.user.User;

public class CreateUserService implements CreateUserUseCase {

    private final UserRepositoryPort userRepository;

    public CreateUserService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User create(String name, String email) {
        return userRepository.save(User.newUser(name, email));
    }
}
