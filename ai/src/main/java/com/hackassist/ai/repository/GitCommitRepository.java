package com.hackassist.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hackassist.ai.models.GitCommit;
import com.hackassist.ai.models.GitHubRepository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GitCommitRepository extends JpaRepository<GitCommit, Long> {
    
    List<GitCommit> findByRepository(GitHubRepository repository);

    void deleteByRepository(GitHubRepository repository);
    
    List<GitCommit> findByRepositoryOrderByCommitDateDesc(GitHubRepository repository);
    
    List<GitCommit> findByCommitDateAfter(LocalDateTime date);
    
    List<GitCommit> findByAuthor(String author);
}
