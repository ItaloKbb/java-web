package br.senai.aula.web.infrastructure.web.card;

import br.senai.aula.web.infrastructure.persistence.card.entity.repository.CardJpaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cards")
public class CardController {

	private final CardJpaRepository cards;

	public CardController(CardJpaRepository cards) {
		this.cards = cards;
	}

	@GetMapping
	public List<CardResponse> list() {
		return cards.findAll().stream()
			.map(c -> new CardResponse(c.getId(), c.getValor(), c.getNaipe(), c.getUrl()))
				.toList();
	}

	@GetMapping("/{id}")
	public CardResponse get(@PathVariable long id) {
		var card = cards.findById(id)
				.orElseThrow(() -> new java.util.NoSuchElementException("Carta não encontrada"));

		return new CardResponse(card.getId(), card.getValor(), card.getNaipe(), card.getUrl());
	}

}
