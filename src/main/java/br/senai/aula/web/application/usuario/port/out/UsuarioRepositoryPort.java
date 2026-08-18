package br.senai.aula.web.application.usuario.port.out;

import br.senai.aula.web.domain.usuario.Usuario;

public interface UsuarioRepositoryPort {

    Usuario salvar(Usuario usuario);
}
