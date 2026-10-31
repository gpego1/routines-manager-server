package br.com.github.gpego1.routines.service;
import br.com.github.gpego1.routines.database.model.CategoryEntity;
import br.com.github.gpego1.routines.database.model.TaskEntity;
import br.com.github.gpego1.routines.database.model.UserEntity;
import br.com.github.gpego1.routines.database.repository.ICategoriesRepository;
import br.com.github.gpego1.routines.database.repository.ITaskRepository;
import br.com.github.gpego1.routines.database.repository.IUserRepository;
import br.com.github.gpego1.routines.dto.requests.CreateTaskRequest;
import br.com.github.gpego1.routines.dto.requests.UpdateTaskRequest;
import br.com.github.gpego1.routines.dto.responses.TaskResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final ITaskRepository taskRepository;
    private final ICategoriesRepository categoriesRepository;
    private final IUserRepository userRepository;

    @Transactional
    public void createTask(CreateTaskRequest dto) {
        CategoryEntity category = categoriesRepository.findByName(dto.category().name())
                .orElseGet(() -> categoriesRepository.save(CategoryEntity.builder().name(dto.category().name()).build())
        );

        UserEntity user = this.findUser(dto.userId());

        taskRepository.save(
                TaskEntity.builder()
                        .name(dto.name())
                        .description(dto.description())
                        .category(category)
                        .user(user)
                        .startDateTime(dto.startDateTime())
                        .endDateTime(dto.endDateTime())
                        .durationHour(dto.endDateTime().getHour() - dto.startDateTime().getHour())
                        .durationMinutes(dto.endDateTime().getMinute() - dto.startDateTime().getMinute())
                        .build()
        );
    }

    public TaskResponseDto getTaskById(Integer id) {
        TaskEntity task = taskRepository.findById(id)
                .orElse(null);

        assert task != null;
        return new TaskResponseDto(
                task.getName(),
                task.getDescription(),
                task.getStartDateTime(),
                task.getEndDateTime(),
                task.getDurationHour(),
                task.getDurationMinutes()
        );
    }

    @Transactional
    public void updateTask(UpdateTaskRequest dto, Integer id) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found.")
                );

        Integer durationHours = dto.endDateTime().getHour() - dto.startDateTime().getHour();
        Integer durationMinutes = dto.endDateTime().getMinute() - dto.startDateTime().getMinute();

        CategoryEntity category = CategoryEntity
                .builder()
                .name(dto.category().name())
                .build();
        categoriesRepository.save(category);

        task.setCategory(category);
        task.setName(dto.name());
        task.setDescription(dto.description());
        task.setStartDateTime(dto.startDateTime());
        task.setEndDateTime(dto.endDateTime());
        task.setDurationHour(durationHours);
        task.setDurationMinutes(durationMinutes);
        taskRepository.save(task);
    }

    public void deleteById(Integer id) {
        taskRepository.deleteById(id);
    }

    private UserEntity findUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not found")
        );
    }


}
