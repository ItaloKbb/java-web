package br.senai.aula.web.infrastructure.web.admin;

import br.senai.aula.web.domain.cards.Naipe;
import br.senai.aula.web.domain.cards.Valor;
import br.senai.aula.web.domain.skills.SkillType;
import br.senai.aula.web.infrastructure.persistence.card.entity.repository.CardJpaRepository;
import br.senai.aula.web.infrastructure.persistence.match.repository.PuzzleChallengeJpaRepository;
import br.senai.aula.web.infrastructure.persistence.puzzle.entity.PuzzleJpaEntity;
import br.senai.aula.web.infrastructure.persistence.puzzle.repository.PuzzleJpaRepository;
import br.senai.aula.web.infrastructure.persistence.skills.entity.SkillsJpaEntity;
import br.senai.aula.web.infrastructure.persistence.skills.repository.SkillsJpaRepository;
import br.senai.aula.web.infrastructure.web.puzzle.response.PuzzleResponse;
import br.senai.aula.web.infrastructure.web.skill.response.SkillResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AdminCatalogService {
    public record SkillInput(String name, String description, SkillType type, Naipe naipe, Valor valor) {}
    public record PuzzleInput(String question, String[] alternativas, Integer alternativaCorreta) {}
    public record AdminPuzzleResponse(Long id, String question, String[] alternativas, Integer alternativaCorreta) {}

    private final CardJpaRepository cards;
    private final SkillsJpaRepository skills;
    private final PuzzleJpaRepository puzzles;
    private final PuzzleChallengeJpaRepository challenges;

    public AdminCatalogService(CardJpaRepository cards, SkillsJpaRepository skills,
                               PuzzleJpaRepository puzzles, PuzzleChallengeJpaRepository challenges) {
        this.cards = cards;
        this.skills = skills;
        this.puzzles = puzzles;
        this.challenges = challenges;
    }

    @Transactional(readOnly = true)
    public List<SkillResponse> skills() {
        return skills.findByArchivedAtIsNull().stream().map(this::skillResponse).toList();
    }

    @Transactional
    public SkillResponse createSkill(SkillInput input) {
        validateSkill(input, null);
        return skillResponse(skills.save(new SkillsJpaEntity(null, input.name().trim(),
                input.description().trim(), input.type(), input.naipe(), input.valor())));
    }

    @Transactional
    public SkillResponse updateSkill(long id, SkillInput input) {
        SkillsJpaEntity skill = activeSkill(id);
        validateSkill(input, id);
        skill.update(input.name().trim(), input.description().trim(), input.type(), input.naipe(), input.valor());
        return skillResponse(skills.save(skill));
    }

    @Transactional
    public void archiveSkill(long id) {
        SkillsJpaEntity skill = activeSkill(id);
        skill.archive();
        skills.save(skill);
    }

    @Transactional(readOnly = true)
    public List<AdminPuzzleResponse> puzzles() {
        return puzzles.findByArchivedAtIsNull().stream().map(this::puzzleResponse).toList();
    }

    @Transactional
    public AdminPuzzleResponse createPuzzle(PuzzleInput input) {
        String[] alternatives = validatePuzzle(input);
        return puzzleResponse(puzzles.save(new PuzzleJpaEntity(input.question().trim(),
                alternatives, input.alternativaCorreta())));
    }

    @Transactional
    public AdminPuzzleResponse updatePuzzle(long id, PuzzleInput input) {
        PuzzleJpaEntity puzzle = activePuzzle(id);
        if (challenges.existsByPuzzleAndAnsweredFalse(puzzle)) {
            throw new IllegalStateException("Este puzzle tem um desafio pendente; tente novamente depois da resposta");
        }
        String[] alternatives = validatePuzzle(input);
        puzzle.update(input.question().trim(), alternatives, input.alternativaCorreta());
        return puzzleResponse(puzzles.save(puzzle));
    }

    @Transactional
    public void archivePuzzle(long id) {
        PuzzleJpaEntity puzzle = activePuzzle(id);
        if (puzzles.findByArchivedAtIsNull().size() <= 1) {
            throw new IllegalStateException("Mantenha pelo menos um puzzle ativo para as partidas");
        }
        puzzle.archive();
        puzzles.save(puzzle);
    }

    private void validateSkill(SkillInput input, Long currentId) {
        if (input == null || input.name() == null || input.name().isBlank() || input.name().trim().length() > 100
                || input.description() == null || input.description().isBlank()
                || input.description().trim().length() > 255 || input.type() == null
                || input.naipe() == null || input.valor() == null) {
            throw new IllegalArgumentException("Informe nome, descrição, tipo e carta válidos");
        }
        if (cards.lockByValorAndNaipe(input.valor(), input.naipe()).isEmpty()) {
            throw new IllegalArgumentException("A carta escolhida não existe");
        }
        skills.findByValorAndNaipeAndArchivedAtIsNull(input.valor(), input.naipe()).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw new IllegalStateException("Esta carta já possui uma skill ativa");
            }
        });
    }

    private String[] validatePuzzle(PuzzleInput input) {
        if (input == null || input.question() == null || input.question().isBlank()
                || input.question().trim().length() > 255 || input.alternativas() == null
                || input.alternativas().length < 2 || input.alternativaCorreta() == null
                || input.alternativaCorreta() < 0 || input.alternativaCorreta() >= input.alternativas().length
                || Arrays.stream(input.alternativas()).anyMatch(item -> item == null || item.isBlank())) {
            throw new IllegalArgumentException("Informe pergunta, duas ou mais alternativas e uma resposta correta válida");
        }
        return Arrays.stream(input.alternativas()).map(String::trim).toArray(String[]::new);
    }

    private SkillsJpaEntity activeSkill(long id) {
        return skills.findById(id).filter(item -> item.getArchivedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("Skill não encontrada"));
    }

    private PuzzleJpaEntity activePuzzle(long id) {
        return puzzles.findById(id).filter(item -> item.getArchivedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("Puzzle não encontrado"));
    }

    private SkillResponse skillResponse(SkillsJpaEntity skill) {
        return new SkillResponse(skill.getId(), skill.getName(), skill.getDescription(),
                skill.getType(), skill.getNaipe(), skill.getValor());
    }

    private AdminPuzzleResponse puzzleResponse(PuzzleJpaEntity puzzle) {
        return new AdminPuzzleResponse(puzzle.getId(), puzzle.getQuestion(),
                puzzle.getAlternativas(), puzzle.getAlternativaCorreta());
    }
}
