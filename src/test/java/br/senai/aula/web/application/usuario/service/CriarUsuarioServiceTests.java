package br.senai.aula.web.application.usuario.service;

import br.senai.aula.web.application.usuario.port.out.UsuarioRepositoryPort;
import br.senai.aula.web.domain.usuario.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CriarUsuarioServiceTests {

    @Test
    void deveCriarESalvarUmUsuario() {
        UsuarioRepositoryPort repositorioEmMemoria = usuario -> {
            assertNull(usuario.getId());
            return new Usuario(1L, usuario.getNome(), usuario.getEmail());
        };
        CriarUsuarioService service = new CriarUsuarioService(repositorioEmMemoria);

        Usuario usuario = service.criar("Maria", "maria@exemplo.com");

        assertEquals(1L, usuario.getId());
        assertEquals("Maria", usuario.getNome());
        assertEquals("maria@exemplo.com", usuario.getEmail());
    }
}
