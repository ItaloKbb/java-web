package br.senai.aula.web.application.user.service;

import br.senai.aula.web.application.port.out.UserRepositoryPort;
import br.senai.aula.web.application.service.CreateUserService;
import br.senai.aula.web.domain.user.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CreateUserServiceTests {

    @Test
    void shouldCreateUserWithEmptyCoinBalance() {
        UserRepositoryPort inMemoryRepository = new UserRepositoryPort() {
            @Override
            public User save(User user) {
                assertNull(user.id());
                assertEquals(BigDecimal.ZERO.setScale(2), user.coin().balance());
                return new User(1L, user.name(), user.email(), user.coin());
            }

            @Override
            public Optional<User> findById(Long id) {
                return Optional.empty();
            }
        };

        User user = new CreateUserService(inMemoryRepository).create("Maria", "maria@example.com");

        assertEquals(1L, user.id());
        assertEquals("Maria", user.name());
        assertEquals(BigDecimal.ZERO.setScale(2), user.coin().balance());
    }
}
