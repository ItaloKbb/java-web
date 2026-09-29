package br.senai.aula.web.infrastructure.persistence.game.adapter;

import br.senai.aula.web.application.port.out.RoundRepositoryPort;
import br.senai.aula.web.domain.game.Round;
import br.senai.aula.web.domain.game.StatusRound;
import br.senai.aula.web.infrastructure.persistence.game.entity.GameJpaEntity;
import br.senai.aula.web.infrastructure.persistence.game.entity.RoundJpaEntity;
import br.senai.aula.web.infrastructure.persistence.game.mapper.RoundPersistenceMapper;
import br.senai.aula.web.infrastructure.persistence.game.repository.GameJpaRepository;
import br.senai.aula.web.infrastructure.persistence.game.repository.RoundJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.NoSuchElementException;

@Repository
public class RoundRepositoryAdapter implements RoundRepositoryPort {

    private final RoundJpaRepository roundRepository;
    private final GameJpaRepository gameRepository;

    public RoundRepositoryAdapter(
            RoundJpaRepository roundRepository,
            GameJpaRepository gameRepository
    ) {
        this.roundRepository = roundRepository;
        this.gameRepository = gameRepository;
    }

    @Override
    @Transactional
    public Round save(Round round, Long gameId) {
        GameJpaEntity game = gameRepository.findById(gameId).orElseThrow(
                () -> new NoSuchElementException("Jogo não encontrado: " + gameId)
        );

        return RoundPersistenceMapper.toDomain(
                roundRepository.save(RoundPersistenceMapper.toEntity(round, game))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Round> findById(Long id) {
        return roundRepository.findById(id).map(RoundPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByGameId(Long gameId) {
        return roundRepository.countByGameId(gameId);
    }

    @Override
    @Transactional
    public Round updateStatus(Long roundId, StatusRound status) {
        RoundJpaEntity entity = roundRepository.findById(roundId)
                .orElseThrow(() -> new NoSuchElementException("Rodada não encontrada: " + roundId));
        entity.setStatus(status);
        return RoundPersistenceMapper.toDomain(roundRepository.save(entity));
    }
}
