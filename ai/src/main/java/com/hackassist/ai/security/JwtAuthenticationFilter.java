package com.hackassist.ai.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        
        log.info("JwtAuthenticationFilter: {} {}", method, requestUri);

        if (isBypassedPath(requestUri)) {
            log.debug("JwtAuthenticationFilter: Bypassed path, skipping JWT validation");
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = getJwtFromRequest(request);
        if (jwt == null) {
            log.warn("JwtAuthenticationFilter: No JWT token found in Authorization header for: {}", requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("JwtAuthenticationFilter: JWT token found, validating...");
        
        if (!tokenProvider.validateToken(jwt)) {
            log.warn("✗ JwtAuthenticationFilter: JWT validation failed");
            filterChain.doFilter(request, response);
            return;
        }
        
        String uid = tokenProvider.getUidFromToken(jwt);
        log.info("✓ JwtAuthenticationFilter: JWT validated for user: {}", uid);
        
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(uid, null, Collections.emptyList());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        log.debug("JwtAuthenticationFilter: SecurityContext set for user: {}", uid);

        filterChain.doFilter(request, response);
    }

    private boolean isBypassedPath(String requestUri) {
        return requestUri.startsWith("/oauth2/")
                || requestUri.startsWith("/login/")
                || requestUri.startsWith("/error");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
