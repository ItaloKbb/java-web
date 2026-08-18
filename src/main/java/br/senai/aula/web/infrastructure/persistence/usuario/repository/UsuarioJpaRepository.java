package br.senai.aula.web.infrastructure.persistence.usuario.repository;

import br.senai.aula.web.infrastructure.persistence.usuario.entity.UsuarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, Long> {
}
