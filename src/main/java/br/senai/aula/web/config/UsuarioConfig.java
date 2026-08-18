package br.senai.aula.web.config;

import br.senai.aula.web.application.usuario.port.in.CriarUsuarioUseCase;
import br.senai.aula.web.application.usuario.port.out.UsuarioRepositoryPort;
import br.senai.aula.web.application.usuario.service.CriarUsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsuarioConfig {

    @Bean
    CriarUsuarioUseCase criarUsuarioUseCase(UsuarioRepositoryPort usuarioRepository) {
        return new CriarUsuarioService(usuarioRepository);
    }
}
