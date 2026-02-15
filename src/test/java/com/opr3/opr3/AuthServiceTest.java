package com.opr3.opr3;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.opr3.opr3.dto.AuthRequest;
import com.opr3.opr3.dto.TokenInfo;
import com.opr3.opr3.entity.Token;
import com.opr3.opr3.entity.User;
import com.opr3.opr3.repository.TokenRepository;
import com.opr3.opr3.repository.UserRepository;
import com.opr3.opr3.service.AuthService;
import com.opr3.opr3.service.AuthUtilService;
import com.opr3.opr3.service.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Mock
    private AuthUtilService authUtilService;

    @InjectMocks
    private AuthService authService;

    private AuthRequest request;
    private User mockUser;
    private String mockJwtToken;
    private String mockRefreshToken;
    private ResponseCookie mockRefreshCookie;
    private List<Token> existingTokens;

    // Initialize common test data before each test
    @BeforeEach
    void setUp() {
        request = new AuthRequest("test@example.com", "password123");

        mockUser = new User("testUser", "password123", "test@example.com");

        mockJwtToken = "mock.jwt.token";
        mockRefreshToken = "mock.refresh.token";

        mockRefreshCookie = ResponseCookie.from("mockRefreshToken", mockRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh")
                .maxAge(100000)
                .sameSite("Strict")
                .build();

        existingTokens = List.of(
                Token.builder().token("old-token-1").revoked(false).build(),
                Token.builder().token("old-token-2").revoked(false).build());
    }

    @Test
    void shouldAuthenticateUserSuccessfully() {
        // when
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        when(jwtService.generateToken(mockUser)).thenReturn(mockJwtToken);
        when(jwtService.generateRefreshToken(mockUser)).thenReturn(mockRefreshToken);
        when(jwtService.createRefreshTokenCookie(mockRefreshToken)).thenReturn(mockRefreshCookie);

        // execute
        TokenInfo result = authService.authenticate(request);

        // verify
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshCookie());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(mockUser);
        verify(jwtService).generateRefreshToken(mockUser);
        verify(jwtService).createRefreshTokenCookie(mockRefreshToken);

        // Verify revokeAllUserTokens was called (indirectly)
        verify(tokenRepository).findAllValidTokenByUser(mockUser.getUid());

        // Verify saveUserToken was called (indirectly)
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenAuthenticationFails() {
        // when
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        // execute
        assertThrows(BadCredentialsException.class, () -> {
            authService.authenticate(request);
        });

        // verify
        verify(jwtService, never()).generateToken(any());
        verify(jwtService, never()).generateRefreshToken(any());
        verify(tokenRepository, never()).save(any(Token.class));
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        // when
        when(authUtilService.getAuthenticatedUser()).thenReturn(mockUser);
        when(jwtService.generateToken(mockUser)).thenReturn(mockJwtToken);
        when(jwtService.generateRefreshToken(mockUser)).thenReturn(mockRefreshToken);
        when(jwtService.createRefreshTokenCookie(mockRefreshToken)).thenReturn(mockRefreshCookie);
        when(tokenRepository.findAllValidTokenByUser(mockUser.getUid())).thenReturn(List.of());

        // execute
        TokenInfo result = authService.refreshToken();

        // verify
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshCookie());

        verify(authUtilService).getAuthenticatedUser();
        verify(jwtService).generateToken(mockUser);
        verify(jwtService).generateRefreshToken(mockUser);
        verify(tokenRepository).findAllValidTokenByUser(mockUser.getUid());
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringRefresh() {
        // when
        when(authUtilService.getAuthenticatedUser()).thenThrow(new RuntimeException("User not found"));

        // execute
        Exception exception = assertThrows(Exception.class, () -> {
            authService.refreshToken();
        });

        // verify
        assertNotNull(exception);
        verify(authUtilService).getAuthenticatedUser();
    }

    @Test
    void shouldRevokeAllUserTokensWhenTokensExist() {
        // when
        when(tokenRepository.findAllValidTokenByUser(mockUser.getUid())).thenReturn(existingTokens);

        // for when calling .authenticate
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        // execute
        authService.authenticate(request); // This calls revokeAllUserTokens internally

        // verify
        verify(tokenRepository).findAllValidTokenByUser(mockUser.getUid());
        verify(tokenRepository).saveAll(argThat(tokens -> {
            List<Token> tokenList = (List<Token>) tokens;
            return tokenList.size() == 2 &&
                    tokenList.stream().allMatch(Token::isRevoked);
        }));
    }

    @Test
    void shouldNotCallSaveAllWhenNoValidTokensExist() {
        // when
        when(tokenRepository.findAllValidTokenByUser(mockUser.getUid())).thenReturn(List.of());

        // for when calling .authenticate
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        // execute
        authService.authenticate(request);

        // verify
        verify(tokenRepository).findAllValidTokenByUser(mockUser.getUid());
        verify(tokenRepository, never()).saveAll(any());
    }
}
