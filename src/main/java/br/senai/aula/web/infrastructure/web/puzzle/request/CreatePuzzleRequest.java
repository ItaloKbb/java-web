package br.senai.aula.web.infrastructure.web.puzzle.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreatePuzzleRequest(
        @NotNull(message = "Adicione as alternativas")
        @Size(min = 2, message = "Adicione pelo menos duas alternativas")
        String[] alternativas,
        @NotNull(message = "Adicione a resposta correta")
        @PositiveOrZero(message = "A resposta correta deve ser uma posição válida")
        Integer alternativaCorreta
) {
}

