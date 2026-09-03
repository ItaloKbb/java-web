package br.senai.aula.web.infrastructure.persistence.skills.adapter;

import br.senai.aula.web.application.port.out.SkillsRepositoryPort;
import br.senai.aula.web.domain.skills.Skills;
import br.senai.aula.web.infrastructure.persistence.skills.entity.SkillsJpaEntity;
import br.senai.aula.web.infrastructure.persistence.skills.mapper.SkillsPersistenceMapper;
import br.senai.aula.web.infrastructure.persistence.skills.repository.SkillsJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SkillsRepositoryAdapter implements SkillsRepositoryPort {

	private final SkillsJpaRepository repository;

	public SkillsRepositoryAdapter(SkillsJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Skills save(Skills skill) {
		SkillsJpaEntity entity = SkillsPersistenceMapper.toEntity(skill);
		return SkillsPersistenceMapper.toDomain(repository.save(entity));
	}

	@Override
	public Optional<Skills> findById(Long id) {
		return repository.findById(id).map(SkillsPersistenceMapper::toDomain);
	}

	@Override
	public List<Skills> findAll() {
		return repository.findAll()
				.stream()
				.map(SkillsPersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public void deleteById(Long id) {
		repository.deleteById(id);
	}
}
