package com.hackassist.ai.security;

import com.hackassist.ai.Service.UserService;
import com.hackassist.ai.models.User;
import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomOidcUserService extends OidcUserService {

    @Autowired
    private UserService userService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        log.info("============ CustomOidcUserService.loadUser() START ============");
        log.info("OIDC Provider: {}", userRequest.getClientRegistration().getRegistrationId());

        OidcUser oidcUser = super.loadUser(userRequest);
        Map<String, Object> attributes = oidcUser.getAttributes();

        log.info("OIDC Attributes received: {}", attributes);

        String uid = toStringSafe(attributes.get("sub"));
        String email = toStringSafe(attributes.get("email"));
        String name = toStringSafe(attributes.get("name"));
        String picture = toStringSafe(attributes.get("picture"));

        log.info("OIDC uid: {}", uid);
        log.info("OIDC email: {}", email);
        log.info("OIDC name: {}", name);

        if (email == null || email.isEmpty()) {
            log.error("ERROR: OIDC email is missing from provider response!");
            throw new IllegalStateException("OIDC email is missing");
        }

        try {
            userService.getUserByEmail(email);
            log.info("User already exists for email: {}", email);
        } catch (RuntimeException ex) {
            String effectiveUid = (uid != null && !uid.isEmpty()) ? uid : email;
            User user = new User(effectiveUid, name, picture, email, "GOOGLE", Instant.now());
            userService.registerUser(user);
            log.info("Created new OIDC user: uid={}, email={}", effectiveUid, email);
        }

        log.info("============ CustomOidcUserService.loadUser() END ============");
        return oidcUser;
    }

    private String toStringSafe(Object value) {
        return value == null ? null : value.toString();
    }
}
