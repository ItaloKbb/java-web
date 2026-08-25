package br.senai.aula.web.domain.cards;

public record Card(
        Long id,
        Valor valor,
        Naipe naipe
) {

    public static Card newCard(
            Valor valor,
            Naipe naipe
    ) {

        return new Card(
                null,
                valor,
                naipe
        );
    }
}