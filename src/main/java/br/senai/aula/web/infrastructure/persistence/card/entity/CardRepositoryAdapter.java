package br.senai.aula.web.infrastructure.card;

import br.senai.aula.web.domain.cards.Card;
import br.senai.aula.web.domain.cards.CardRepository;
import br.senai.aula.web.infrastructure.persistence.card.entity.CardEntity;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CardRepositoryAdapter implements CardRepository {

    private final CardJpaRepository repository;

    public CardRepositoryAdapter(CardJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Card save(Card card) {

        CardEntity entity = CardMapper.toEntity(card);

        entity = repository.save(entity);

        return CardMapper.toDomain(entity);
    }

    @Override
    public Optional<Card> findById(Long id) {

        return repository.findById(id)
                .map(CardMapper::toDomain);
    }

    @Override
    public List<Card> findAll() {

        return repository.findAll()
                .stream()
                .map(CardMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {

        repository.deleteById(id);

    }

}