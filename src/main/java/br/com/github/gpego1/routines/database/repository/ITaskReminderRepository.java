package br.com.github.gpego1.routines.database.repository;

import br.com.github.gpego1.routines.database.model.TaskReminderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ITaskReminderRepository extends JpaRepository<TaskReminderEntity, Integer> {
    java.util.List<TaskReminderEntity> findByTaskId(Integer taskId);
    void deleteByTaskId(Integer taskId);
    @org.springframework.data.jpa.repository.Query("select r.task.id from TaskReminderEntity r where r.id = :id")
    java.util.Optional<Integer> findTaskIdByReminderId(Integer id);
    @org.springframework.data.jpa.repository.Query("select r.id from TaskReminderEntity r where r.status = br.com.github.gpego1.routines.enums.ReminderStatusTypeEnum.PENDING and r.scheduledAt <= :now order by r.scheduledAt, r.id")
    java.util.List<Integer> findDueIds(java.time.Instant now, org.springframework.data.domain.Pageable page);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select r from TaskReminderEntity r where r.id = :id")
    java.util.Optional<TaskReminderEntity> lockById(Integer id);
}
