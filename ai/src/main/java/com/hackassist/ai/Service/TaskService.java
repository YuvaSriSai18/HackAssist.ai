package com.hackassist.ai.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hackassist.ai.models.Tasks;
import com.hackassist.ai.models.User;
import com.hackassist.ai.repository.TaskRepository;
import com.hackassist.ai.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    public Tasks createTask(Tasks task) {
        if(task.getAssignedTo() == null) {
            throw new RuntimeException("Task must be assigned to a user");
        }
        if(!userRepository.existsById(task.getAssignedTo().getUid())) {
            throw new RuntimeException("User not found with id: " + task.getAssignedTo().getUid());
        }
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public Tasks getTaskById(Long id) {
        Optional<Tasks> task = taskRepository.findById(id);
        if(!task.isPresent()) {
            throw new RuntimeException("Task not found with id: " + id);
        }
        return task.get();
    }

    public Tasks updateTask(Tasks task) {
        if(!taskRepository.existsById(task.getId())) {
            throw new RuntimeException("Task not found with id: " + task.getId());
        }
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        if(!taskRepository.existsById(id)) {
            throw new RuntimeException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    public List<Tasks> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<Tasks> getTasksByUser(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if(!user.isPresent()) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        return taskRepository.findByAssignedToOrderByCreatedAtDesc(user.get());
    }

    public void completeTask(Long taskId) {
        Tasks task = getTaskById(taskId);
        task.setStatus(com.hackassist.ai.models.TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        updateTask(task);
    }

    public void startTask(Long taskId) {
        Tasks task = getTaskById(taskId);
        task.setStatus(com.hackassist.ai.models.TaskStatus.IN_PROGRESS);
        updateTask(task);
    }

    public void blockTask(Long taskId, String reason) {
        Tasks task = getTaskById(taskId);
        task.setStatus(com.hackassist.ai.models.TaskStatus.BLOCKED);
        updateTask(task);
    }

    public List<Tasks> getTasksByStatus(String status) {
        return taskRepository.findByStatusOrderByPriorityDesc(status);
    }
}

