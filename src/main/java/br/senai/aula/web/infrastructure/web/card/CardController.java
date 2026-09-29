package br.senai.aula.web.infrastructure.web.card;
import br.senai.aula.web.infrastructure.persistence.card.entity.repository.CardJpaRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/cards") public class CardController {private final CardJpaRepository cards;public CardController(CardJpaRepository cards){this.cards=cards;}@GetMapping public List<CardResponse> list(){return cards.findAll().stream().map(c->new CardResponse(c.getId(),c.getValor(),c.getNaipe())).toList();}@GetMapping("/{id}") public CardResponse get(@PathVariable long id){var c=cards.findById(id).orElseThrow(()->new java.util.NoSuchElementException("Carta não encontrada"));return new CardResponse(c.getId(),c.getValor(),c.getNaipe());}}
