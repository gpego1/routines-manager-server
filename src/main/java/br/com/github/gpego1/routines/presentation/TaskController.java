package br.com.github.gpego1.routines.presentation;
import br.com.github.gpego1.routines.dto.requests.CreateTaskRequest;
import br.com.github.gpego1.routines.dto.requests.UpdateTaskRequest;
import br.com.github.gpego1.routines.dto.responses.CreatedTaskResponse;
import br.com.github.gpego1.routines.dto.responses.TaskResponseDto;
import br.com.github.gpego1.routines.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<CreatedTaskResponse> createTask(@RequestBody @Valid CreateTaskRequest req) {
        return new ResponseEntity<>(taskService.createTask(req), HttpStatus.CREATED);
    }

    @PostMapping("/finish/{id}")
    public ResponseEntity<CreatedTaskResponse> finishTask(@PathVariable Integer id) {
        return new ResponseEntity<>(taskService.finishTask(id), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Integer id) {
        return new ResponseEntity<>(taskService.getTaskById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateById(@PathVariable Integer id, @RequestBody @Valid UpdateTaskRequest req) {
        taskService.updateTask(req, id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Integer id) {
        taskService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskResponseDto>> findTasksByUserId(@PathVariable Integer userId) {
        return new ResponseEntity<>(taskService.findTasksByUserId(userId), HttpStatus.OK);
    }

    @GetMapping("/progress/{progressId}")
    public ResponseEntity<List<TaskResponseDto>> findTasksByProgressId(@PathVariable Integer progressId) {
        return new ResponseEntity<>(taskService.findTaskByProgressId(progressId), HttpStatus.OK);
    }
}
