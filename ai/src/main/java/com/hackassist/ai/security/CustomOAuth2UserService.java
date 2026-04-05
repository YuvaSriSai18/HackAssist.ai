package com.hackassist.ai.security;

import com.hackassist.ai.Service.UserService;
import com.hackassist.ai.models.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;

@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        log.info("============ CustomOAuth2UserService.loadUser() START ============");
        log.info("OAuth2 Provider: {}", userRequest.getClientRegistration().getRegistrationId());
        
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        log.info("OAuth2 Attributes received: {}", attributes.keySet());

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");
        String sub = (String) attributes.get("sub");

        log.info("OAuth2 Email: {}", email);
        log.info("OAuth2 Name: {}", name);
        log.info("OAuth2 Sub: {}", sub);

        if (email == null || email.isEmpty()) {
            log.error("ERROR: OAuth2 email is missing from provider response!");
            throw new IllegalStateException("OAuth2 email is missing");
        }

        User user;
        try {
            log.info("Attempting to find existing user by email: {}", email);
            user = userService.getUserByEmail(email);
            log.info("Found existing user: {}", user.getUid());
        } catch (RuntimeException ex) {
            log.info("User not found, creating new OAuth2 user");
            String uid = (sub != null && !sub.isEmpty()) ? sub : email;
            user = new User(uid, name, picture, email, "GOOGLE", Instant.now());
            userService.registerUser(user);
            log.info("✓ Created new OAuth2 user: uid={}, email={}", uid, email);
        }

        CustomUserPrincipal principal = new CustomUserPrincipal(user, attributes);
        log.info("============ CustomOAuth2UserService.loadUser() END - User loaded successfully ============");
        return principal;
    }
}
