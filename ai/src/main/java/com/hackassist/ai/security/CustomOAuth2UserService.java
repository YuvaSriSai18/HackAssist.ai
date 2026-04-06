package com.hackassist.ai.security;

import com.hackassist.ai.Service.UserService;
import com.hackassist.ai.models.User;
import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

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

        String email = toStringSafe(attributes.get("email"));
        String name = toStringSafe(attributes.get("name"));
        String picture = toStringSafe(attributes.get("picture"));
        String sub = toStringSafe(attributes.get("sub"));

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

    private String toStringSafe(Object value) {
        return value == null ? null : value.toString();
    }
}
