package com.hackassist.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
@Slf4j
public class OAuth2ClientConfig {

    @Value("${GOOGLE_CLIENT_ID:}")
    private String googleClientId;

    @Value("${GOOGLE_CLIENT_SECRET:}")
    private String googleClientSecret;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        log.info("============ OAuth2ClientConfig: Initializing ClientRegistrationRepository ============");
        log.info("Google Client ID present: {}", (googleClientId != null && !googleClientId.isEmpty()));
        log.info("Google Client Secret present: {}", (googleClientSecret != null && !googleClientSecret.isEmpty()));
        if (googleClientId == null || googleClientId.isEmpty()) {
            log.error("ERROR: GOOGLE_CLIENT_ID is not set in environment!");
        }
        if (googleClientSecret == null || googleClientSecret.isEmpty()) {
            log.error("ERROR: GOOGLE_CLIENT_SECRET is not set in environment!");
        }
        return new InMemoryClientRegistrationRepository(googleClientRegistration());
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            ClientRegistrationRepository clientRegistrationRepository) {
        log.info("OAuth2ClientConfig: Creating OAuth2AuthorizedClientService");
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    private ClientRegistration googleClientRegistration() {
        log.info("OAuth2ClientConfig: Building Google ClientRegistration");
        ClientRegistration reg = ClientRegistration.withRegistrationId("google")
                .clientId(googleClientId)
                .clientSecret(googleClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .scope("openid", "profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
            .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
            .issuerUri("https://accounts.google.com")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .clientName("Google")
                .build();
        log.info("OAuth2ClientConfig: Google ClientRegistration created successfully");
        return reg;
    }
}
