package com.hackassist.ai.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
        String name = null;
        String picture = null;

        if (authentication.getPrincipal() instanceof CustomUserPrincipal) {
            CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
            uid = principal.getUser().getUid();
            email = principal.getEmail();
            name = principal.getUser().getName();
            picture = principal.getUser().getPhotoUrl();
            log.info("Principal extracted - uid: {}, email: {}", uid, email);
        } else if (authentication.getPrincipal() instanceof OidcUser) {
            OidcUser principal = (OidcUser) authentication.getPrincipal();
            uid = principal.getAttribute("sub");
            email = principal.getAttribute("email");
            name = principal.getAttribute("name");
            picture = principal.getAttribute("picture");
            if (uid == null || uid.isEmpty()) {
                uid = principal.getName();
            }
            if (email == null || email.isEmpty()) {
                email = uid;
            }
            log.info("OIDC principal extracted - uid: {}, email: {}", uid, email);
            log.info("OIDC attributes: {}", principal.getAttributes());
        } else if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User principal = (OAuth2User) authentication.getPrincipal();
            uid = principal.getAttribute("sub");
            email = principal.getAttribute("email");
            name = principal.getAttribute("name");
            picture = principal.getAttribute("picture");
            if (uid == null || uid.isEmpty()) {
                uid = principal.getName();
            }
            if (email == null || email.isEmpty()) {
                email = uid;
            }
            log.info("OAuth2 principal extracted - uid: {}, email: {}", uid, email);
            log.info("OAuth2 attributes: {}", principal.getAttributes());
        } else {
            log.warn("Principal is not CustomUserPrincipal, using authentication name: {}", uid);
        }

        String token = tokenProvider.generateToken(uid, email);

        String redirectUrl = "http://localhost:5173/auth/callback?token="
            + URLEncoder.encode(token, StandardCharsets.UTF_8)
            + "&user=" + URLEncoder.encode(uid, StandardCharsets.UTF_8)
            + "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
            + "&name=" + URLEncoder.encode(name == null ? "" : name, StandardCharsets.UTF_8)
            + "&picture=" + URLEncoder.encode(picture == null ? "" : picture, StandardCharsets.UTF_8);

        log.info("✓ OAuth2 authentication successful!");
        log.info("Redirecting to frontend: http://localhost:5173/auth/callback");
        response.sendRedirect(redirectUrl);
    }
}
