package br.com.github.gpego1.routines.service;
import br.com.github.gpego1.routines.database.model.CategoryEntity;
import br.com.github.gpego1.routines.database.model.TaskEntity;
import br.com.github.gpego1.routines.database.model.UserEntity;
import br.com.github.gpego1.routines.database.repository.ICategoriesRepository;
import br.com.github.gpego1.routines.database.repository.ITaskRepository;
import br.com.github.gpego1.routines.database.repository.IUserRepository;
import br.com.github.gpego1.routines.dto.requests.CreateTaskRequest;
import br.com.github.gpego1.routines.dto.requests.UpdateTaskRequest;
import br.com.github.gpego1.routines.dto.responses.CreatedTaskResponse;
import br.com.github.gpego1.routines.dto.responses.TaskResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final ITaskRepository taskRepository;
    private final ICategoriesRepository categoriesRepository;
    private final IUserRepository userRepository;
    private final TaskReminderService reminderService;

    @Transactional
    public CreatedTaskResponse createTask(CreateTaskRequest dto) {
        CategoryEntity category = CategoryEntity.builder()
                .name(dto.category().name())
                .build();

        categoriesRepository.save(category);

        UserEntity user = this.findUser(dto.userId());

        if (!this.isDurationValid(dto.startDateTime(), dto.endDateTime())) {
            throw new RuntimeException("Invalid date instructions.");
        }

        TaskEntity task =  taskRepository.save(
                TaskEntity.builder()
                        .name(dto.name())
                        .description(dto.description())
                        .category(category)
                        .finished(false)
                        .user(user)
                        .startDateTime(dto.startDateTime())
                        .endDateTime(dto.endDateTime())
                        .durationHour(dto.endDateTime().getHour() - dto.startDateTime().getHour())
                        .durationMinutes(dto.endDateTime().getMinute() - dto.startDateTime().getMinute())
                        .build()
        );
        reminderService.schedule(task);
        return new CreatedTaskResponse(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getFinished(),
                task.getDurationHour(),
                task.getDurationMinutes()
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
    public CreatedTaskResponse finishTask(Integer id) {
        TaskEntity task = taskRepository.lockById(id)
                .orElseThrow(() -> new RuntimeException("task not found.")
        );

        task.setFinished(true);
        reminderService.cancel(id);
        task.setEndDateTime(LocalDateTime.now());
        task.setDurationHour(task.getDurationHour() - LocalDateTime.now().getHour());
        task.setDurationMinutes(task.getDurationMinutes() - LocalDateTime.now().getMinute());
        taskRepository.save(task);

        return new CreatedTaskResponse(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getFinished(),
                task.getDurationHour(),
                task.getDurationMinutes()
        );

    }

    @Transactional
    public void updateTask(UpdateTaskRequest dto, Integer id) {
        TaskEntity task = taskRepository.lockById(id)
                .orElseThrow(() -> new RuntimeException("Task not found.")
                );

        if (!this.isDurationValid(dto.startDateTime(), dto.endDateTime())) {
            throw new RuntimeException("Invalid date instructions.");
        }

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
        if (Boolean.TRUE.equals(task.getFinished())) reminderService.cancel(id);
        else reminderService.schedule(task);
    }

    public List<TaskResponseDto> findTasksByUserId(Integer userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found.")
        );
        List<TaskEntity> tasks = taskRepository.findTaskByUserId(userId);
        if (tasks.isEmpty()) {return null;}

        return tasks.stream()
                .map((taskEntity) ->
                        new TaskResponseDto(taskEntity.getName(), taskEntity.getDescription(), taskEntity.getStartDateTime(),
                                taskEntity.getEndDateTime(), taskEntity.getDurationHour(), taskEntity.getDurationMinutes())
                )
                .toList();
    }

    public List<TaskResponseDto> findFinishedTaskByUserId(Integer userId) {
        return taskRepository.findByUserIdWhereFinishedIsTrue(userId).stream()
                .map(task -> new TaskResponseDto(task.getName(), task.getDescription(), task.getStartDateTime(), task.getEndDateTime(), task.getDurationHour(), task.getDurationMinutes()))
                .toList();
    }

    @Transactional
    public void deleteById(Integer id) {
        taskRepository.lockById(id).orElseThrow(() -> new IllegalArgumentException("Task not found"));
        reminderService.delete(id);
        taskRepository.deleteById(id);
    }

    private UserEntity findUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not found")
        );
    }


    private boolean isDurationValid(LocalDateTime start, LocalDateTime end) {
        if (start.isBefore(LocalDateTime.now())) return false;

        if (end.getHour() - start.getHour() < 0) return false;
        if (end.getDayOfMonth() != start.getDayOfMonth()) return false;

        if (start.isAfter(end)) {
            return false;
        } else {
            return true;
        }
    }


}
