package com.hackassist.ai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.hackassist.ai.models.Tasks;
import com.hackassist.ai.models.TaskStatus;
import com.hackassist.ai.models.TaskPriority;
import com.hackassist.ai.models.User;
import com.hackassist.ai.Service.TaskService;
import com.hackassist.ai.Service.UserService;
import com.hackassist.ai.dto.TaskDTO;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskDTO taskDTO) {
        try {
            User user = userService.getUserById(taskDTO.getAssignedToUid());
            Tasks task = new Tasks(
                taskDTO.getTitle(),
                taskDTO.getDescription(),
                user,
                TaskStatus.valueOf(taskDTO.getStatus() != null ? taskDTO.getStatus() : "PENDING"),
                TaskPriority.valueOf(taskDTO.getPriority() != null ? taskDTO.getPriority() : "MEDIUM"),
                taskDTO.getDueDate(),
                taskDTO.getEstimatedHours()
            );
            Tasks savedTask = taskService.createTask(task);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id) {
        try {
            Tasks task = taskService.getTaskById(id);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllTasks() {
        try {
            List<Tasks> tasks = taskService.getAllTasks();
            return ResponseEntity.ok(tasks);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve tasks: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getTasksByUser(@PathVariable String userId) {
        try {
            List<Tasks> tasks = taskService.getTasksByUser(userId);
            return ResponseEntity.ok(tasks);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO) {
        try {
            Tasks task = taskService.getTaskById(id);
            task.setTitle(taskDTO.getTitle());
            task.setDescription(taskDTO.getDescription());
            task.setStatus(TaskStatus.valueOf(taskDTO.getStatus()));
            task.setPriority(TaskPriority.valueOf(taskDTO.getPriority()));
            task.setDueDate(taskDTO.getDueDate());
            task.setEstimatedHours(taskDTO.getEstimatedHours());
            Tasks updatedTask = taskService.updateTask(task);
            return ResponseEntity.ok(updatedTask);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Task deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeTask(@PathVariable Long id) {
        try {
            taskService.completeTask(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Task completed successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<?> startTask(@PathVariable Long id) {
        try {
            taskService.startTask(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Task started successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PutMapping("/{id}/block")
    public ResponseEntity<?> blockTask(@PathVariable Long id, @RequestParam String reason) {
        try {
            taskService.blockTask(id, reason);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Task blocked successfully");
            response.put("reason", reason);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getTasksByStatus(@PathVariable String status) {
        try {
            List<Tasks> tasks = taskService.getTasksByStatus(status);
            return ResponseEntity.ok(tasks);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
