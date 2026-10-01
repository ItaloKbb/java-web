package br.senai.aula.web.infrastructure.web.card;

import br.senai.aula.web.infrastructure.persistence.card.entity.repository.CardJpaRepository;
import br.senai.aula.web.infrastructure.persistence.card.entity.entity.CardEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

	@PutMapping("/{id}")
	public CardResponse update(
			@PathVariable long id,
			@Valid @RequestBody CreateCardRequest request
	) {
		var card = cards.findById(id)
				.orElseThrow(() -> new java.util.NoSuchElementException("Carta não encontrada"));

		card.setValor(request.valor());
		card.setNaipe(request.naipe());
		card.setUrl(request.url());

		var updatedCard = cards.save(card);
		return new CardResponse(updatedCard.getId(), updatedCard.getValor(), updatedCard.getNaipe(), updatedCard.getUrl());
	}

	@PutMapping("/bulk")
	@Transactional
	public List<CardResponse> bulkUpdate(@Valid @RequestBody List<@Valid CardResponse> requests) {
		Map<Long, CardResponse> uniqueRequests = requests.stream()
				.collect(Collectors.toMap(request -> request.id(), request -> request, (first, second) -> {
					throw new IllegalArgumentException("Carta duplicada no envio: " + first.id());
				}));

		Map<Long, CardEntity> cardsById = cards.findAllById(uniqueRequests.keySet()).stream()
				.collect(Collectors.toMap(card -> card.getId(), card -> card));

		if (cardsById.size() != uniqueRequests.size()) {
			throw new java.util.NoSuchElementException("Uma ou mais cartas não foram encontradas");
		}

		uniqueRequests.forEach((id, request) -> {
			var card = cardsById.get(id);
			card.setValor(request.valor());
			card.setNaipe(request.naipe());
			card.setUrl(request.url());
		});

		return cards.saveAll(cardsById.values()).stream()
				.map(card -> new CardResponse(card.getId(), card.getValor(), card.getNaipe(), card.getUrl()))
				.toList();
	}
}
