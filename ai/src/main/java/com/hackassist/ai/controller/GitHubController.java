package com.hackassist.ai.controller;

import com.hackassist.ai.Service.IGitHubService;
import com.hackassist.ai.models.GitHubUser;
import com.hackassist.ai.models.User;
import com.hackassist.ai.repository.GitHubUserRepository;
import com.hackassist.ai.security.JwtTokenProvider;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.Collections;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/github")
@Slf4j
public class GitHubController {

    @Value("${GITHUB_CLIENT_ID:}")
    private String githubClientId;

    @Value("${GITHUB_CLIENT_SECRET:}")
    private String githubClientSecret;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private GitHubUserRepository gitHubUserRepository;

    @Autowired
    private IGitHubService githubService;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @GetMapping("/connect")
    public ResponseEntity<?> connect(@RequestHeader(value = "Authorization", required = false) String authorization,
                                     @RequestParam(value = "token", required = false) String token) {
        String jwt = extractToken(authorization, token);
        if (jwt == null || !tokenProvider.validateToken(jwt)) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        String state = URLEncoder.encode(jwt, StandardCharsets.UTF_8);
        String redirectUrl = "https://github.com/login/oauth/authorize?client_id="
                + URLEncoder.encode(githubClientId, StandardCharsets.UTF_8)
                + "&scope=repo%20user"
                + "&state=" + state;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", redirectUrl);
        return new ResponseEntity<>(headers, org.springframework.http.HttpStatus.FOUND);
    }

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code,
                                      @RequestParam("state") String state) {
        String jwt = state == null ? null : state;
        if (jwt == null || !tokenProvider.validateToken(jwt)) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        String uid = tokenProvider.getUidFromToken(jwt);
        User user = githubService.getUserByUid(uid);

        Map<String, Object> tokenResponse = exchangeCodeForToken(code);
        String accessToken = tokenResponse.get("access_token").toString();
        Instant expiresAt = extractExpiry(tokenResponse.get("expires_in"));
        Map<String, Object> githubProfile = fetchGitHubProfile(accessToken);

        String githubId = githubProfile.get("id") == null ? null : githubProfile.get("id").toString();
        String login = githubProfile.get("login") == null ? null : githubProfile.get("login").toString();
        String name = githubProfile.get("name") == null ? null : githubProfile.get("name").toString();
        String avatarUrl = githubProfile.get("avatar_url") == null ? null : githubProfile.get("avatar_url").toString();

        GitHubUser githubUser = gitHubUserRepository.findByUserUid(user.getUid())
                .orElseGet(GitHubUser::new);
        githubUser.setUser(user);
        githubUser.setGithubId(githubId == null ? login : githubId);
        githubUser.setGithubUsername(login);
        githubUser.setName(name);
        githubUser.setAvatarUrl(avatarUrl);
        githubUser.setGithubVerified(true);
        githubUser.setGithubConnected(true);
        gitHubUserRepository.save(githubUser);

        saveAuthorizedClient(uid, accessToken, expiresAt);

        String frontendRedirect = "http://localhost:5173/auth/callback?github=connected";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", frontendRedirect);
        return new ResponseEntity<>(headers, org.springframework.http.HttpStatus.FOUND);
    }

    @GetMapping("/repos")
    public ResponseEntity<?> getRepositories(@RequestHeader("Authorization") String authorization) {
        String uid = resolveUidFromAuthorization(authorization);
        if (uid == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }
        List<Map<String, Object>> repos = githubService.fetchUserRepositoriesByUid(uid);
        return ResponseEntity.ok(repos);
    }

    @GetMapping("/commits/{owner}/{repo}")
    public ResponseEntity<?> getCommits(@RequestHeader("Authorization") String authorization,
                                        @PathVariable String owner,
                                        @PathVariable String repo) {
        String uid = resolveUidFromAuthorization(authorization);
        if (uid == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }
        List<Map<String, Object>> commits = githubService.fetchCommitsByUid(uid, owner, repo);
        return ResponseEntity.ok(commits);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getGitHubUser(@RequestHeader("Authorization") String authorization) {
        String uid = resolveUidFromAuthorization(authorization);
        if (uid == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        GitHubUser githubUser = gitHubUserRepository.findByUserUid(uid).orElse(null);
        Map<String, Object> response = new HashMap<>();
        if (githubUser == null) {
            response.put("connected", false);
            return ResponseEntity.ok(response);
        }

        response.put("connected", githubUser.isGithubConnected());
        response.put("githubId", githubUser.getGithubId());
        response.put("githubUsername", githubUser.getGithubUsername());
        response.put("name", githubUser.getName());
        response.put("avatarUrl", githubUser.getAvatarUrl());
        response.put("githubVerified", githubUser.isGithubVerified());
        return ResponseEntity.ok(response);
    }

    private String extractToken(String authorization, String token) {
        if (token != null && !token.isEmpty()) {
            return token;
        }
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    private String resolveUidFromAuthorization(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring(7);
        if (!tokenProvider.validateToken(token)) {
            return null;
        }
        return tokenProvider.getUidFromToken(token);
    }

    private Map<String, Object> exchangeCodeForToken(String code) {
        String url = "https://github.com/login/oauth/access_token";
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> payload = new HashMap<>();
        payload.put("client_id", githubClientId);
        payload.put("client_secret", githubClientSecret);
        payload.put("code", code);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> body = response.getBody();
        if (body == null || body.get("access_token") == null) {
            throw new IllegalStateException("GitHub access token missing");
        }
        return body;
    }

    private void saveAuthorizedClient(String uid, String accessToken, Instant expiresAt) {
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("github");
        if (registration == null) {
            throw new IllegalStateException("GitHub client registration not found");
        }
        OAuth2AccessToken token = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                accessToken,
                Instant.now(),
                expiresAt
        );
        OAuth2AuthorizedClient client = new OAuth2AuthorizedClient(registration, uid, token);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(uid, null, Collections.emptyList());
        authorizedClientService.saveAuthorizedClient(client, authentication);
    }

    private Instant extractExpiry(Object expiresInValue) {
        if (expiresInValue == null) {
            return Instant.now().plusSeconds(3600);
        }
        try {
            long seconds = Long.parseLong(expiresInValue.toString());
            return Instant.now().plusSeconds(seconds);
        } catch (NumberFormatException ex) {
            return Instant.now().plusSeconds(3600);
        }
    }

    private Map<String, Object> fetchGitHubProfile(String accessToken) {
        String url = "https://api.github.com/user";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        return response.getBody() == null ? Map.of() : response.getBody();
    }
}
