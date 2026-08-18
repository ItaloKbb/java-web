package br.senai.aula.web.infrastructure.persistence.usuario.adapter;

import br.senai.aula.web.application.usuario.port.out.UsuarioRepositoryPort;
import br.senai.aula.web.domain.usuario.Usuario;
import br.senai.aula.web.infrastructure.persistence.usuario.entity.UsuarioJpaEntity;
import br.senai.aula.web.infrastructure.persistence.usuario.mapper.UsuarioPersistenceMapper;
import br.senai.aula.web.infrastructure.persistence.usuario.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository repository;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        UsuarioJpaEntity entidade = UsuarioPersistenceMapper.paraEntidade(usuario);
        return UsuarioPersistenceMapper.paraDominio(repository.save(entidade));
    }
}
