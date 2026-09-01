package br.senai.aula.web.infrastructure.persistence.user.adapter;

import br.senai.aula.web.application.port.out.UserRepositoryPort;
import br.senai.aula.web.domain.user.User;
import br.senai.aula.web.infrastructure.persistence.user.mapper.UserPersistenceMapper;
import br.senai.aula.web.infrastructure.persistence.user.repository.UserJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository repository;

    public UserRepositoryAdapter(UserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public User save(User user) {
        return UserPersistenceMapper.toDomain(repository.save(UserPersistenceMapper.toEntity(user)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return repository.findById(id).map(UserPersistenceMapper::toDomain);
    }
}
