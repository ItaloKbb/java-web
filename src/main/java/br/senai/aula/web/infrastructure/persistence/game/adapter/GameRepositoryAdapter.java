package br.senai.aula.web.infrastructure.persistence.game.adapter;

import br.senai.aula.web.application.port.out.GameRepositoryPort;
import br.senai.aula.web.domain.game.Game;
import br.senai.aula.web.infrastructure.persistence.game.mapper.GamePersistenceMapper;
import br.senai.aula.web.infrastructure.persistence.game.repository.GameJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class GameRepositoryAdapter implements GameRepositoryPort {

    private final GameJpaRepository repository;

    public GameRepositoryAdapter(GameJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Game save(Game game) {
        return GamePersistenceMapper.toDomain(
                repository.save(GamePersistenceMapper.toEntity(game))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Game> findById(Long id) {
        return repository.findById(id).map(GamePersistenceMapper::toDomain);
    }
}
