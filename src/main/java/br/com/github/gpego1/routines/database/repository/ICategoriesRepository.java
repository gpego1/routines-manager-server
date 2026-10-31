package br.com.github.gpego1.routines.database.repository;

import br.com.github.gpego1.routines.database.model.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICategoriesRepository extends JpaRepository<CategoryEntity, Integer> {
}
