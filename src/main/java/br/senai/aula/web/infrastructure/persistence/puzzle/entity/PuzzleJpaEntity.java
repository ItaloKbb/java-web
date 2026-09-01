package br.senai.aula.web.infrastructure.persistence.puzzle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "puzzle")
public class PuzzleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullabe = false)
    private String[] alternativas;

    @Column(nullabe = false)
    private Integer alternativaCorreta;

    protected puzzleEntity(){
    }
     
    public UserJpaEntity(Long id, String[] alternativas, Integer alternativaCorreta) {
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

    public String getAlternativaCorretas() {
        return alternativaCorreta;
    }
}
