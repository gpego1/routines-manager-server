package br.com.github.gpego1.routines.database.repository;
import br.com.github.gpego1.routines.database.model.ProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IProgressRepository extends JpaRepository<ProgressEntity, Integer> {
    @Query(value = """
        SELECT * FROM progress p
        WHERE p.user_id = :userId
    """, nativeQuery = true)
    Optional<ProgressEntity> findProgressByUserId(@Param("userId") Integer userId);
}
