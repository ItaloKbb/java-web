package br.senai.aula.web.infrastructure.web.puzzle.controller;
import br.senai.aula.web.application.puzzle.port.in.CreatePuzzleUseCase;
import br.senai.aula.web.application.puzzle.port.in.GetPuzzleUseCase;
import br.senai.aula.web.domain.puzzle.Puzzle;
import br.senai.aula.web.infrastructure.web.puzzle.request.CreatePuzzleRequest;
import br.senai.aula.web.infrastructure.web.puzzle.response.PuzzleResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/puzzles")
public class PuzzleController {

    private final CreatePuzzleUseCase createPuzzleUseCase;
    private final GetPuzzleUseCase getPuzzleUseCase;

    public PuzzleController(CreatePuzzleUseCase createPuzzleUseCase, GetPuzzleUseCase getPuzzleUseCase) {
        this.createPuzzleUseCase = createPuzzleUseCase;
        this.getPuzzleUseCase = getPuzzleUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PuzzleResponse create(@Valid @RequestBody CreatePuzzleRequest request) {
        Puzzle puzzle = createPuzzleUseCase.create(request.alternativas(), request.alternativaCorreta());
        return PuzzleResponse.from(puzzle);
    }

    @GetMapping("/{puzzleId}")
    public PuzzleResponse getPuzzle(@PathVariable Long puzzleId) {
        Puzzle puzzle = getPuzzleUseCase.findById(puzzleId);
        return PuzzleResponse.from(puzzle);
    }

    @GetMapping
    public List<PuzzleResponse> getPuzzles() {
        return Arrays.stream(getPuzzleUseCase.findAll())
                .map(PuzzleResponse::from)
                .toList();
    }
}
