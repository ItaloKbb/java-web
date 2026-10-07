package br.senai.aula.web.infrastructure.persistence.card.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.senai.aula.web.infrastructure.persistence.card.entity.entity.CardEntity;
import br.senai.aula.web.domain.cards.Naipe;
import br.senai.aula.web.domain.cards.Valor;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface CardJpaRepository extends JpaRepository<CardEntity, Long> {
    Optional<CardEntity> findByValorAndNaipe(Valor valor, Naipe naipe);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select card from CardEntity card where card.valor = :valor and card.naipe = :naipe")
    Optional<CardEntity> lockByValorAndNaipe(@Param("valor") Valor valor, @Param("naipe") Naipe naipe);
}
