package com.hackassist.ai.repository;

import com.hackassist.ai.models.GitHubUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GitHubUserRepository extends JpaRepository<GitHubUser, String> {
    Optional<GitHubUser> findByUserUid(String uid);
}
