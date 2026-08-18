package br.senai.aula.web.config;

import br.senai.aula.web.application.user.port.in.CreateUserUseCase;
import br.senai.aula.web.application.user.port.in.GetUserCoinUseCase;
import br.senai.aula.web.application.user.port.out.UserRepositoryPort;
import br.senai.aula.web.application.user.service.CreateUserService;
import br.senai.aula.web.application.user.service.GetUserCoinService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfig {

    @Bean
    CreateUserUseCase createUserUseCase(UserRepositoryPort userRepository) {
        return new CreateUserService(userRepository);
    }

    @Bean
    GetUserCoinUseCase getUserCoinUseCase(UserRepositoryPort userRepository) {
        return new GetUserCoinService(userRepository);
    }
}
