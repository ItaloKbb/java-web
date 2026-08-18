package br.senai.aula.web.infrastructure.persistence.user;

import br.senai.aula.web.domain.user.User;
import br.senai.aula.web.infrastructure.persistence.user.adapter.UserRepositoryAdapter;
import br.senai.aula.web.infrastructure.persistence.user.repository.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class UserCoinJpaTests {

    private final UserRepositoryAdapter adapter;
    private final UserJpaRepository repository;

    @Autowired
    UserCoinJpaTests(UserRepositoryAdapter adapter, UserJpaRepository repository) {
        this.adapter = adapter;
        this.repository = repository;
    }

    @Test
    void shouldPersistOneCoinRowForTheUser() {
        User saved = adapter.save(User.newUser("Maria", "maria@example.com"));

        assertNotNull(saved.id());
        assertNotNull(saved.coin().id());
        assertEquals(BigDecimal.ZERO.setScale(2), saved.coin().balance());
        assertEquals(saved.coin().id(), repository.findById(saved.id()).orElseThrow().getCoin().getId());
    }
}
