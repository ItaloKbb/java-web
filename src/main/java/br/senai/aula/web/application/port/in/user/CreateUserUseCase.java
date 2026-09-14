package br.senai.aula.web.application.port.in.user;

import br.senai.aula.web.domain.user.User;

public interface CreateUserUseCase {

    User create(String name, String email);
}
