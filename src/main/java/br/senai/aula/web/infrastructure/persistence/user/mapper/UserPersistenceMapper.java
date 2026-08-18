package br.senai.aula.web.infrastructure.persistence.user.mapper;

import br.senai.aula.web.domain.user.Coin;
import br.senai.aula.web.domain.user.User;
import br.senai.aula.web.infrastructure.persistence.user.entity.CoinJpaEntity;
import br.senai.aula.web.infrastructure.persistence.user.entity.UserJpaEntity;

public final class UserPersistenceMapper {

    private UserPersistenceMapper() {
    }

    public static UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity(user.id(), user.name(), user.email());
        Coin coin = user.coin();
        entity.setCoin(new CoinJpaEntity(coin.id(), coin.balance()));
        return entity;
    }

    public static User toDomain(UserJpaEntity entity) {
        CoinJpaEntity coin = entity.getCoin();
        return new User(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                new Coin(coin.getId(), coin.getBalance())
        );
    }
}
