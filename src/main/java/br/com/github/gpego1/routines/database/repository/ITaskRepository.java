package br.com.github.gpego1.routines.database.repository;

import br.com.github.gpego1.routines.database.model.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ITaskRepository extends JpaRepository<TaskEntity, Integer> {
    @Query(value = """
        SELECT * FROM tasks t
        WHERE t.user_id = :userId
    """, nativeQuery = true)
    List<TaskEntity> findTaskByUserId(@Param("userId") Integer userId);
}
