package br.senai.aula.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI webOpenApi() {
        return new OpenAPI()
            .servers(java.util.List.of(new Server().url("/")))
                .info(new Info()
                        .title("Truno Crazzy API")
                        .description("API para usuários, cartas, habilidades, puzzles e partidas")
                        .version("v1"));
    }
}
