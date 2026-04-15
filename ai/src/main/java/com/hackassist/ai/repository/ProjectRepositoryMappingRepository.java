package com.hackassist.ai.repository;

import com.hackassist.ai.models.ProjectRepositoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProjectRepositoryMappingRepository extends JpaRepository<ProjectRepositoryMapping, Long> {
    Optional<ProjectRepositoryMapping> findByRepoFullName(String repoFullName);
}
