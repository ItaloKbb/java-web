package br.senai.aula.web.infrastructure.persistence.puzzle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "puzzle")
public class PuzzleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String[] alternativas;

    @Column(nullable = false)
    private Integer alternativaCorreta;

    protected PuzzleJpaEntity() {
    }

    public PuzzleJpaEntity(Long id, String[] alternativas, Integer alternativaCorreta) {
        this.id = id;
        this.alternativas = alternativas;
        this.alternativaCorreta = alternativaCorreta;
    }

    public Long getId() {
        return id;
    }

    public String[] getAlternativas() {
        return alternativas;
    }

    public Integer getAlternativaCorreta() {
        return alternativaCorreta;
    }
}
