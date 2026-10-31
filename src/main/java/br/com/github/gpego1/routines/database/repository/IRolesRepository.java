package br.com.github.gpego1.routines.database.repository;

import br.com.github.gpego1.routines.database.model.RolesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRolesRepository extends JpaRepository<RolesEntity, Integer> {
}
