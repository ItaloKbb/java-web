package br.senai.aula.web.infrastructure.persistence.usuario.mapper;

import br.senai.aula.web.domain.usuario.Usuario;
import br.senai.aula.web.infrastructure.persistence.usuario.entity.UsuarioJpaEntity;

public final class UsuarioPersistenceMapper {

    private UsuarioPersistenceMapper() {
    }

    public static UsuarioJpaEntity paraEntidade(Usuario usuario) {
        return new UsuarioJpaEntity(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }

    public static Usuario paraDominio(UsuarioJpaEntity entidade) {
        return new Usuario(entidade.getId(), entidade.getNome(), entidade.getEmail());
    }
}
