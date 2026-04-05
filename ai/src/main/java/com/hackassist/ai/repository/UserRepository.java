package com.hackassist.ai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.hackassist.ai.models.User;

public interface UserRepository extends JpaRepository<User , String>{

    User findByEmail(String email); 

    User getUserByEmail(String email) ;

    User getUserByUid(String uid) ;

    @Query("SELECT u FROM User u")
    List<User> getAllUsers() ;
}