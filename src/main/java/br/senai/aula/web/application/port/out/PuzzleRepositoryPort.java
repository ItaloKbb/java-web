package br.senai.aula.web.application.port.out;

import br.senai.aula.web.domain.puzzle.Puzzle;

import java.util.Optional;

public interface PuzzleRepositoryPort {

    Puzzle save(Puzzle puzzle);

    Optional<Puzzle> findById(Long id);

    Optional<Puzzle[]> findAll();
}