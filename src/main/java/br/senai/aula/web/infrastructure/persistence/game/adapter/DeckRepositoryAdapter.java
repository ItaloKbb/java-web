package br.senai.aula.web.infrastructure.persistence.game.adapter;

import br.senai.aula.web.application.port.out.DeckRepositoryPort;
import br.senai.aula.web.domain.game.Deck;
import br.senai.aula.web.infrastructure.persistence.game.mapper.DeckPersistenceMapper;
import br.senai.aula.web.infrastructure.persistence.game.repository.DeckJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class DeckRepositoryAdapter implements DeckRepositoryPort {

    private final DeckJpaRepository repository;

    public DeckRepositoryAdapter(DeckJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Deck save(Deck deck) {
        return DeckPersistenceMapper.toDomain(
                repository.save(DeckPersistenceMapper.toEntity(deck))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Deck> findById(Long id) {
        return repository.findById(id).map(DeckPersistenceMapper::toDomain);
    }
}
