package com.hackassist.ai.Service;

import com.hackassist.ai.dto.CurrentUserDTO;
import com.hackassist.ai.models.User;
import com.hackassist.ai.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Slf4j
public class AuthService implements IAuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public CurrentUserDTO getCurrentUser(String username) {
        try {
            Optional<User> user = userRepository.findById(username);
            
            if (user.isPresent()) {
                User userData = user.get();
                CurrentUserDTO dto = new CurrentUserDTO();
                dto.setUid(userData.getUid());
                dto.setName(userData.getName());
                dto.setEmail(userData.getEmail());
                dto.setPhotoUrl(userData.getPhotoUrl());
                dto.setAuthenticated(true);
                return dto;
            } else {
                log.warn("User not found with username: {}", username);
                return new CurrentUserDTO(null, null, null, null, false);
            }
        } catch (Exception e) {
            log.error("Error fetching current user: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch current user");
        }
    }
    
    @Override
    public Boolean isUserAuthenticated(String username) {
        try {
            return userRepository.existsById(username);
        } catch (Exception e) {
            log.error("Error checking user authentication: {}", e.getMessage());
            return false;
        }
    }
}
