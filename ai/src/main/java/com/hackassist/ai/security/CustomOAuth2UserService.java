package com.hackassist.ai.security;

import com.hackassist.ai.Service.UserService;
import com.hackassist.ai.models.GitHubUser;
import com.hackassist.ai.models.User;
import com.hackassist.ai.repository.GitHubUserRepository;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserService userService;

    @Autowired
    private GitHubUserRepository gitHubUserRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        log.info("============ CustomOAuth2UserService.loadUser() START ============");
        log.info("OAuth2 Provider: {}", userRequest.getClientRegistration().getRegistrationId());

        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 Attributes received: {}", attributes.keySet());

        if ("github".equalsIgnoreCase(registrationId)) {
            handleGitHubUser(attributes);
            return oAuth2User;
        }

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

    private void handleGitHubUser(Map<String, Object> attributes) {
        log.info("============ GitHub OAuth user handling START ============");
        log.info("GitHub Attributes received: {}", attributes);

        String githubId = toStringSafe(attributes.get("id"));
        String login = toStringSafe(attributes.get("login"));
        String name = toStringSafe(attributes.get("name"));
        String avatarUrl = toStringSafe(attributes.get("avatar_url"));
        String email = toStringSafe(attributes.get("email"));

        if (email == null || email.isEmpty()) {
            String safeLogin = (login == null || login.isEmpty()) ? "github-user" : login.toLowerCase(Locale.ROOT);
            email = safeLogin + "@users.noreply.github.com";
            log.warn("GitHub email is missing; using fallback email: {}", email);
        }

        User user = resolveLinkedUser(email, githubId, name, avatarUrl);

        GitHubUser githubUser = gitHubUserRepository.findByUserUid(user.getUid())
            .orElseGet(GitHubUser::new);
        githubUser.setUser(user);
        githubUser.setGithubUsername(login);
        githubUser.setGithubId(githubId);
        githubUser.setGithubVerified(true);
        githubUser.setAvatarUrl(avatarUrl);
        githubUser.setName(name);
        gitHubUserRepository.save(githubUser);

        log.info("Saved GitHub user mapping: uid={}, username={}", user.getUid(), login);
        log.info("============ GitHub OAuth user handling END ============");
    }

    private String toStringSafe(Object value) {
        return value == null ? null : value.toString();
    }

    private User resolveLinkedUser(String email, String githubId, String name, String avatarUrl) {
        String connectUid = getConnectUidFromSession();
        if (connectUid != null && !connectUid.isEmpty()) {
            log.info("Using connect session uid to link GitHub account: {}", connectUid);
            return userService.getUserById(connectUid);
        }

        try {
            User user = userService.getUserByEmail(email);
            log.info("Linked GitHub account to existing user uid: {}", user.getUid());
            return user;
        } catch (RuntimeException ex) {
            String uid = (githubId != null && !githubId.isEmpty()) ? githubId : email;
            User user = new User(uid, name, avatarUrl, email, "GITHUB", Instant.now());
            userService.registerUser(user);
            log.info("Created new user for GitHub login: uid={}, email={}", uid, email);
            return user;
        }
    }

    private String getConnectUidFromSession() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        if (request == null || request.getSession(false) == null) {
            return null;
        }
        Object value = request.getSession(false).getAttribute("github_connect_uid");
        if (value == null) {
            return null;
        }
        request.getSession(false).removeAttribute("github_connect_uid");
        return value.toString();
    }
}
