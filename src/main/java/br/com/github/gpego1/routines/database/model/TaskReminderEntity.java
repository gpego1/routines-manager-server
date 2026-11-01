package br.com.github.gpego1.routines.database.model;

import br.com.github.gpego1.routines.enums.ReminderStatusTypeEnum;
import br.com.github.gpego1.routines.enums.ReminderTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "task_reminder", indexes = {
        @Index(name = "idx_reminder_due", columnList = "status,scheduled_at"),
        @Index(name = "idx_reminder_task", columnList = "task_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskReminderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id")
    private TaskEntity task;

    @Enumerated(EnumType.STRING)
    private ReminderTypeEnum type;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Enumerated(EnumType.STRING)
    private ReminderStatusTypeEnum status;

    private Instant sent;
    private Integer attempts;



}
