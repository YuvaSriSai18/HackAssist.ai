package com.hackassist.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hackassist.ai.models.GitHubRepository;
import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.User;
import java.util.List;

@Repository
public interface GitHubRepositoryRepository extends JpaRepository<GitHubRepository, Long> {
    
    List<GitHubRepository> findByUser(User user);
    
    List<GitHubRepository> findByProject(Project project);

    void deleteByProject(Project project);
    
    GitHubRepository findByRepoNameAndOwner(String repoName, String owner);
}
