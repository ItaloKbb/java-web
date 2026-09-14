package br.senai.aula.web.infrastructure.persistence.game.adapter;

import br.senai.aula.web.application.port.out.PlayersRepositoryPort;
import br.senai.aula.web.domain.game.Players;
import br.senai.aula.web.infrastructure.persistence.game.entity.GameJpaEntity;
import br.senai.aula.web.infrastructure.persistence.game.entity.PlayersJpaEntity;
import br.senai.aula.web.infrastructure.persistence.game.mapper.PlayersPersistenceMapper;
import br.senai.aula.web.infrastructure.persistence.game.repository.GameJpaRepository;
import br.senai.aula.web.infrastructure.persistence.game.repository.PlayersJpaRepository;
import br.senai.aula.web.infrastructure.persistence.user.entity.UserJpaEntity;
import br.senai.aula.web.infrastructure.persistence.user.repository.UserJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class PlayersRepositoryAdapter implements PlayersRepositoryPort {

    private final PlayersJpaRepository playersRepository;
    private final GameJpaRepository gameRepository;
    private final UserJpaRepository userRepository;

    public PlayersRepositoryAdapter(
            PlayersJpaRepository playersRepository,
            GameJpaRepository gameRepository,
            UserJpaRepository userRepository
    ) {
        this.playersRepository = playersRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Players add(Long gameId, Long userId) {
        GameJpaEntity game = gameRepository.findById(gameId).orElseThrow(
                () -> new IllegalArgumentException("Jogo não encontrado: " + gameId)
        );

        UserJpaEntity user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("Usuário não encontrado: " + userId)
        );

        PlayersJpaEntity entity = new PlayersJpaEntity(null, user, game);
        return PlayersPersistenceMapper.toDomain(playersRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Players> findByGameId(Long gameId) {
        return playersRepository.findByGameId(gameId)
                .stream()
                .map(PlayersPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByGameId(Long gameId) {
        return playersRepository.countByGameId(gameId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByGameIdAndUserId(Long gameId, Long userId) {
        return playersRepository.existsByGameIdAndUserId(gameId, userId);
    }
}
