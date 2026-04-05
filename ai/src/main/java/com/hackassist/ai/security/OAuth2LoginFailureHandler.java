package com.hackassist.ai.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                       org.springframework.security.core.AuthenticationException exception) 
            throws IOException, ServletException {
        log.error("OAuth2 authentication failed: {}", exception.getMessage());
        
        String frontendUrl = "http://localhost:5173/auth/callback?error=" + exception.getMessage();
        response.sendRedirect(frontendUrl);
    }
}
