package br.senai.aula.web.infrastructure.web.card;

import br.senai.aula.web.application.cards.CreateCardUseCase;
import br.senai.aula.web.application.cards.DeleteCardUseCase;
import br.senai.aula.web.application.cards.FindCardByIdUseCase;
import br.senai.aula.web.application.cards.ListCardsUseCase;
import br.senai.aula.web.domain.cards.Card;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cards")
public class CardController {

    private final CreateCardUseCase createCardUseCase;
    private final FindCardByIdUseCase findCardByIdUseCase;
    private final ListCardsUseCase listCardsUseCase;
    private final DeleteCardUseCase deleteCardUseCase;

    public CardController(
            CreateCardUseCase createCardUseCase,
            FindCardByIdUseCase findCardByIdUseCase,
            ListCardsUseCase listCardsUseCase,
            DeleteCardUseCase deleteCardUseCase
    ) {
        this.createCardUseCase = createCardUseCase;
        this.findCardByIdUseCase = findCardByIdUseCase;
        this.listCardsUseCase = listCardsUseCase;
        this.deleteCardUseCase = deleteCardUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponse create(@Valid @RequestBody CreateCardRequest request) {
        Card card = createCardUseCase.create(request.valor(), request.naipe());
        return CardResponse.from(card);
    }

    @GetMapping
    public List<CardResponse> list() {
        return listCardsUseCase.listAll()
                .stream()
                .map(CardResponse::from)
                .toList();
    }

    @GetMapping("/{cardId}")
    public CardResponse findById(@PathVariable Long cardId) {
        return CardResponse.from(findCardByIdUseCase.findById(cardId));
    }

    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long cardId) {
        deleteCardUseCase.deleteById(cardId);
    }
}
