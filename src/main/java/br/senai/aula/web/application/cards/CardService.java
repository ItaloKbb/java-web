package br.senai.aula.web.application.cards;

import br.senai.aula.web.domain.cards.Card;
import br.senai.aula.web.domain.cards.CardRepository;
import br.senai.aula.web.domain.cards.Naipe;
import br.senai.aula.web.domain.cards.Valor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CardService implements CreateCardUseCase, FindCardByIdUseCase, ListCardsUseCase, DeleteCardUseCase {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Override
    public Card create(Valor valor, Naipe naipe) {
        return cardRepository.save(Card.newCard(valor, naipe));
    }

    @Override
    public Card findById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Carta não encontrada com id: " + id));
    }

    @Override
    public List<Card> listAll() {
        return cardRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        findById(id);
        cardRepository.deleteById(id);
    }
}
