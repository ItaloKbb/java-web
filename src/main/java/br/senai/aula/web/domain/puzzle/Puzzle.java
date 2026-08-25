package br.senai.aula.web.domain.puzzle;

public record Puzzle(Long id, String[] alternativas, Integer alternativaCorreta) {
    public static Puzzle newPuzzle(String[] alternativas, Integer alternativaCorreta){return new Puzzle(null, alternativas, alternativaCorreta);}
}
