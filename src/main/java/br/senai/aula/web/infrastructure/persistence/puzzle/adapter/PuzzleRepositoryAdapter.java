package br.senai.aula.web.infrastructure.persistence.puzzle.adapter;

import br.senai.aula.web.application.puzzle.port.out.PuzzleRepositoryPort;
import br.senai.aula.web.domain.puzzle.Puzzle;
import br.senai.aula.web.infrastructure.persistence.puzzle.mapper.PuzzlePersistenceMapper;
import br.senai.aula.web.infrastructure.persistence.puzzle.repository.PuzzleJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class PuzzleRepositoryAdapter implements PuzzleRepositoryPort {

    private final PuzzleJpaRepository repository;

    public PuzzleRepositoryAdapter(PuzzleJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Puzzle save(Puzzle puzzle) {
        return PuzzlePersistenceMapper.toDomain(repository.save(PuzzlePersistenceMapper.toEntity(puzzle)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Puzzle> findById(Long id) {
        return repository.findById(id).map(PuzzlePersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Puzzle[]> findAll() {
        return Optional.of(repository.findAll()
                .stream()
                .map(PuzzlePersistenceMapper::toDomain)
                .toArray(Puzzle[]::new));
    }
}
