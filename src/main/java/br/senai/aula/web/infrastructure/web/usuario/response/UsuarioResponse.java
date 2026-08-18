package br.senai.aula.web.infrastructure.web.usuario.response;

import br.senai.aula.web.domain.usuario.Usuario;

public record UsuarioResponse(Long id, String nome, String email) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
