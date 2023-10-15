package dev.freddonega.todolist.task;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.freddonega.todolist.utils.Utils;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel task, HttpServletRequest request) {
        task.setUserId((UUID)request.getAttribute("userId"));

        var currentDate = LocalDateTime.now();

        if(currentDate.isAfter(task.getStartAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body("StartAt must be after current date");
        }

        if(task.getEndAt().isBefore(task.getStartAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                    .body("EndAt must be after StartAt");
        }

        var createdTask = this.taskRepository.save(task);

        return ResponseEntity.status(HttpStatus.OK).body(createdTask);
    }

    @GetMapping("/")
    public ResponseEntity list(HttpServletRequest request) {
        var userId = (UUID)request.getAttribute("userId");

        var tasks = this.taskRepository.findByUserId(userId);

        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity update(@RequestBody TaskModel task, HttpServletRequest request, @PathVariable UUID taskId) {
        var userId = (UUID)request.getAttribute("userId");

        var taskExists = this.taskRepository.findById(taskId).orElse(null);

        if(taskExists == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }

        if(!taskExists.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to update this task");
        }
        
        Utils.copyNonNullProperties(task, taskExists);

        var updatedTask = this.taskRepository.save(taskExists);

        return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
    }
}
