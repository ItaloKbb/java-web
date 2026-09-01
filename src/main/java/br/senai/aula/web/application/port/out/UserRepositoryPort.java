package br.senai.aula.web.application.port.out;

import br.senai.aula.web.domain.user.User;

import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Long id);
}
