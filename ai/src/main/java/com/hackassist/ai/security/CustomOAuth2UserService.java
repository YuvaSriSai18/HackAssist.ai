package com.hackassist.ai.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import com.hackassist.ai.models.User;
import com.hackassist.ai.Service.UserService;
import java.util.Optional;

@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    @Autowired
    private UserService userService;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        try {
            // Extract user information from OAuth2 response
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String picture = oAuth2User.getAttribute("picture");
            String id = oAuth2User.getAttribute("sub");
            
            log.info("Loading user from OAuth2: email={}, name={}", email, name);
            
            // Check if user exists, if not create new user
            try {
                User existingUser = userService.getUserByEmail(email);
                return oAuth2User;
            } catch (RuntimeException e) {
                // User doesn't exist, create new user
                User newUser = new User();
                newUser.setUid(id);
                newUser.setEmail(email);
                newUser.setName(name);
                newUser.setPhotoUrl(picture);
                
                userService.registerUser(newUser);
                log.info("New user created via OAuth2: {}", email);
            }
            
            return oAuth2User;
        } catch (Exception e) {
            log.error("Error loading OAuth2 user: {}", e.getMessage());
            throw new RuntimeException("Failed to load OAuth2 user", e);
        }
    }
}
