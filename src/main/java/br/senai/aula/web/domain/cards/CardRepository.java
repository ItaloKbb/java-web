package br.senai.aula.web.domain.cards;

import java.util.List;
import java.util.Optional;

public interface CardRepository {

    Card save(Card card);

    Optional<Card> findById(Long id);

    List<Card> findAll();

    void deleteById(Long id);

}