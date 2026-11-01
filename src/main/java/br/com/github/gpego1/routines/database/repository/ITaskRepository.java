package br.com.github.gpego1.routines.database.repository;

import br.com.github.gpego1.routines.database.model.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ITaskRepository extends JpaRepository<TaskEntity, Integer> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TaskEntity t where t.id = :id")
    Optional<TaskEntity> lockById(Integer id);
    @Query(value = """
        SELECT * FROM tasks t
        WHERE t.user_id = :userId
    """, nativeQuery = true)
    List<TaskEntity> findTaskByUserId(@Param("userId") Integer userId);

    @Query(value = """
        SELECT * FROM tasks t
        WHERE      t.user_id = :userId
        AND      t.finished = 1
    """, nativeQuery = true)
    List<TaskEntity> findByUserIdWhereFinishedIsTrue(@Param("userId") Integer userId);
}
