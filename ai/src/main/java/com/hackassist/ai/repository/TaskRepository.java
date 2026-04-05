package com.hackassist.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hackassist.ai.models.Tasks;
import com.hackassist.ai.models.User;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Tasks, Long> {
    
    List<Tasks> findByAssignedTo(User user);
    
    List<Tasks> findByAssignedToOrderByCreatedAtDesc(User user);
    
    List<Tasks> findByStatusOrderByPriorityDesc(String status);
    
    List<Tasks> findAll();
}
