package br.senai.aula.web.infrastructure.persistence.card.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.senai.aula.web.infrastructure.persistence.card.entity.entity.CardEntity;
import br.senai.aula.web.domain.cards.Naipe;
import br.senai.aula.web.domain.cards.Valor;
import java.util.Optional;


public interface CardJpaRepository extends JpaRepository<CardEntity, Long> {
    Optional<CardEntity> findByValorAndNaipe(Valor valor, Naipe naipe);
}
