package br.senai.aula.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI webOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Usuários")
                        .description("Documentação dos endpoints da aplicação")
                        .version("v1"));
    }
}
