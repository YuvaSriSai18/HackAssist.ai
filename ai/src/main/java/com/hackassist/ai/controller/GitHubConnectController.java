package com.hackassist.ai.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.hackassist.ai.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

@Controller
@Slf4j
public class GitHubConnectController {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @GetMapping("/auth/github/connect")
    public void connectGitHub(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uid = resolveUidFromRequest(request);
        if (uid == null) {
            log.warn("GitHub connect attempted without valid token");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Authentication required");
            return;
        }

        request.getSession(true).setAttribute("github_connect_uid", uid);
        log.info("GitHub connect started for uid: {}", uid);

        response.sendRedirect("/oauth2/authorization/github");
    }

    private String resolveUidFromRequest(HttpServletRequest request) {
        String token = request.getParameter("token");
        if (!StringUtils.hasText(token)) {
            String header = request.getHeader("Authorization");
            if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
                token = header.substring(7);
            }
        }
        if (!StringUtils.hasText(token) || !tokenProvider.validateToken(token)) {
            return null;
        }
        return tokenProvider.getUidFromToken(token);
    }
}
