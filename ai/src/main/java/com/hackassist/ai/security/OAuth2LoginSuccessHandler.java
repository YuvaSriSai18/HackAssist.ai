package com.hackassist.ai.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.hackassist.ai.security.CustomUserPrincipal;

@Component
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("============ OAuth2LoginSuccessHandler.onAuthenticationSuccess() ============");
        
        String uid = authentication.getName();
        String email = uid;

        if (authentication.getPrincipal() instanceof CustomUserPrincipal) {
            CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
            uid = principal.getUser().getUid();
            email = principal.getEmail();
            log.info("Principal extracted - uid: {}, email: {}", uid, email);
        } else {
            log.warn("Principal is not CustomUserPrincipal, using authentication name: {}", uid);
        }

        String token = tokenProvider.generateToken(uid, email);

        String redirectUrl = "http://localhost:5173/auth/callback?token="
                + URLEncoder.encode(token, StandardCharsets.UTF_8)
                + "&user=" + URLEncoder.encode(uid, StandardCharsets.UTF_8)
                + "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);

        log.info("✓ OAuth2 authentication successful!");
        log.info("Redirecting to frontend: http://localhost:5173/auth/callback");
        response.sendRedirect(redirectUrl);
    }
}
