package com.opr3.opr3.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opr3.opr3.dto.auth.AuthRequest;
import com.opr3.opr3.dto.auth.AuthResponse;
import com.opr3.opr3.dto.auth.RegisterRequest;
import com.opr3.opr3.dto.auth.TokenInfo;
import com.opr3.opr3.service.auth.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

        private static final Logger log = LoggerFactory.getLogger(AuthController.class);

        private final AuthService authService;

        @PostMapping("/authenticate")
        public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest request,
                        HttpServletRequest httpServletRequest,
                        HttpServletResponse httpServletResponse) {

                TokenInfo tokens = authService.authenticate(request);

                httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, tokens.getRefreshCookie().toString());

                AuthResponse response = new AuthResponse(tokens.getAccessToken());

                log.info("[{}] user logged in", 200);

                return ResponseEntity.ok(response);
        }

        @PostMapping("/register")
        public ResponseEntity<String> register(@RequestBody RegisterRequest request,
                        HttpServletRequest httpServletRequest,
                        HttpServletResponse httpServletResponse) {

                authService.register(request);
                log.info("[{}] user registered", 201);
                return ResponseEntity.status(201).body("registration successful");
        }

        @PostMapping("/refresh")
        public ResponseEntity<AuthResponse> refresh(HttpServletRequest httpServletRequest,
                        HttpServletResponse httpServletResponse) {
                TokenInfo tokenInfo = authService.refreshToken();
                httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, tokenInfo.getRefreshCookie().toString());
                AuthResponse response = new AuthResponse(tokenInfo.getAccessToken());

                log.info("[{}] token refreshed", 200);

                return ResponseEntity.ok(response);
        }

        @GetMapping("/validateToken")
        public ResponseEntity<String> validateToken(HttpServletRequest httpServletRequest) {
                log.info("[{}] token validated", 200);

                return ResponseEntity.ok("valid");
        }

        @PostMapping("/logout")
        public ResponseEntity<String> logout(HttpServletRequest httpServletRequest) {
                authService.logout();
                log.info("[{}] logout successful", 200);
                return ResponseEntity.ok("logout successful");
        }

}
