package br.senai.aula.web.domain.game;

public enum statusRound{
    EM_ANDAMENTO, FINALIZADO
}

public record Round(Long id, Integer number, statusRound Status) {

    public static Round newRound(Integer number, statusRound Status) {

        return new Round(null, number, Status);
    }
}