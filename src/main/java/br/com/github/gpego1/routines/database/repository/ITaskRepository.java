package br.com.github.gpego1.routines.database.repository;

import br.com.github.gpego1.routines.database.model.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ITaskRepository extends JpaRepository<TaskEntity, Integer> {
}
