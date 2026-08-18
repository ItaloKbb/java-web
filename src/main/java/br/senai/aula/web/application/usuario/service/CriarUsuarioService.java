package br.senai.aula.web.application.usuario.service;

import br.senai.aula.web.application.usuario.port.in.CriarUsuarioUseCase;
import br.senai.aula.web.application.usuario.port.out.UsuarioRepositoryPort;
import br.senai.aula.web.domain.usuario.Usuario;

public class CriarUsuarioService implements CriarUsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;

    public CriarUsuarioService(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario criar(String nome, String email) {
        Usuario usuario = Usuario.novo(nome, email);
        return usuarioRepository.salvar(usuario);
    }
}
