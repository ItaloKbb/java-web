package br.senai.aula.web.infrastructure.persistence.card.entity.entity;

import br.senai.aula.web.domain.cards.Naipe;
import br.senai.aula.web.domain.cards.Valor;
import jakarta.persistence.*;

@Entity
@Table(name = "cards")
public class CardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Valor valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Naipe naipe;

    private String url;

    public CardEntity(Long id, Valor valor, Naipe naipe, String url) {
        this.id = id;
        this.valor = valor;
        this.naipe = naipe;
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public Valor getValor() {
        return valor;
    }

    public Naipe getNaipe() {
        return naipe;
    }

    public String getUrl() {
        return url;
    }
    protected CardEntity() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setValor(Valor valor) {
        this.valor = valor;
    }

    public void setNaipe(Naipe naipe) {
        this.naipe = naipe;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}