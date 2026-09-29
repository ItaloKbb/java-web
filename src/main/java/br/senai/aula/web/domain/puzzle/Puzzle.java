package br.senai.aula.web.domain.puzzle;

public record Puzzle(Long id, String[] alternativas, Integer alternativaCorreta) {

    public Puzzle {
        if (alternativas == null || alternativas.length < 2) {
            throw new IllegalArgumentException("O puzzle deve ter pelo menos duas alternativas");
        }

        if (alternativaCorreta == null
                || alternativaCorreta < 0
                || alternativaCorreta >= alternativas.length) {
            throw new IllegalArgumentException("A alternativa correta deve indicar uma posição existente");
        }
    }

    public static Puzzle newPuzzle(String[] alternativas, Integer alternativaCorreta){return new Puzzle(null, alternativas, alternativaCorreta);}
}
