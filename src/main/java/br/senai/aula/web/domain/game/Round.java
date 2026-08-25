package br.senai.aula.web.domain.game;

enum StatusRound{
    EM_ANDAMENTO, FINALIZADO
}

public record Round(Long id, Integer number, StatusRound Status) {

    public static Round newRound(Integer number, StatusRound Status) {

        return new Round(null, number, Status);
    }
}