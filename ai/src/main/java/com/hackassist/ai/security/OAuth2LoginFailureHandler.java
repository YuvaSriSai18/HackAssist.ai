package com.hackassist.ai.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        org.springframework.security.core.AuthenticationException exception)
            throws IOException, ServletException {
        log.error("============ OAuth2LoginFailureHandler.onAuthenticationFailure() ============");
        log.error("✗ OAuth2 authentication FAILED!");
        log.error("Error: {}", exception.getMessage());
        log.error("Exception class: {}", exception.getClass().getName());
        exception.printStackTrace();
        
        String error = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
        String redirectUrl = "http://localhost:5173/auth/callback?error=" + error;
        
        log.error("Redirecting to frontend with error: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
