package br.senai.aula.web.application.usuario.port.in;

import br.senai.aula.web.domain.usuario.Usuario;

public interface CriarUsuarioUseCase {

    Usuario criar(String nome, String email);
}
