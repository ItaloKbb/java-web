package br.senai.aula.web.infrastructure.web.puzzle.controller;
import br.senai.aula.web.infrastructure.persistence.puzzle.repository.PuzzleJpaRepository;
import br.senai.aula.web.infrastructure.web.puzzle.response.PuzzleResponse;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/puzzles") public class PuzzleController {private final PuzzleJpaRepository puzzles;public PuzzleController(PuzzleJpaRepository puzzles){this.puzzles=puzzles;}@GetMapping public List<PuzzleResponse> list(){return puzzles.findAll().stream().map(p->new PuzzleResponse(p.getId(),p.getQuestion(),p.getAlternativas())).toList();}@GetMapping("/{id}") public PuzzleResponse get(@PathVariable long id){var p=puzzles.findById(id).orElseThrow(()->new java.util.NoSuchElementException("Puzzle não encontrado"));return new PuzzleResponse(p.getId(),p.getQuestion(),p.getAlternativas());}}
