package br.com.github.gpego1.routines.database.model;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressEntity {
    @Id
    @GeneratedValue
    private Integer id;

    private Integer level;
    private Integer streak;

    @OneToMany(mappedBy = "progress")
    private Set<TaskEntity> tasks;

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

}
