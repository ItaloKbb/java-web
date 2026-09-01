package br.senai.aula.web.config;

import br.senai.aula.web.application.port.in.CreateUserUseCase;
import br.senai.aula.web.application.port.in.GetUserCoinUseCase;
import br.senai.aula.web.application.port.out.UserRepositoryPort;
import br.senai.aula.web.application.service.CreateUserService;
import br.senai.aula.web.application.service.GetUserCoinService;
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
