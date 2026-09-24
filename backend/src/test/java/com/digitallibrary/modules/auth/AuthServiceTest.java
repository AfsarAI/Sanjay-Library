package com.digitallibrary.modules.auth;

import com.digitallibrary.core.errors.ConflictException;
import com.digitallibrary.core.errors.UnauthorizedException;
import com.digitallibrary.core.security.JwtTokenProvider;
import com.digitallibrary.core.security.UserPrincipal;
import com.digitallibrary.modules.auth.dto.LoginRequest;
import com.digitallibrary.modules.auth.dto.RegisterRequest;
import com.digitallibrary.modules.auth.dto.TokenResponse;
import com.digitallibrary.modules.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                tokenProvider,
                3600000L,
                2592000000L
        );
    }

    @Test
    @DisplayName("Should successfully register a new student")
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setLibraryId(1L);
        request.setFullName("Rahul Kumar");
        request.setPhoneNumber("9876543210");
        request.setPassword("Secret@123");

        when(userRepository.existsByPhoneNumber("9876543210")).thenReturn(false);
        when(passwordEncoder.encode("Secret@123")).thenReturn("hashedPassword");

        User savedUser = new User(1L, "9876543210", null, "hashedPassword", "Rahul Kumar", UserRole.ROLE_STUDENT);
        savedUser.setId(10L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenProvider.generateToken(any(UserPrincipal.class))).thenReturn("jwt.token.here");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        TokenResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt.token.here", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Rahul Kumar", response.getUser().getFullName());
        assertEquals(UserRole.ROLE_STUDENT, response.getUser().getRole());
    }

    @Test
    @DisplayName("Should throw ConflictException when registering duplicate phone")
    void register_DuplicatePhone_ThrowsConflict() {
        RegisterRequest request = new RegisterRequest();
        request.setPhoneNumber("9876543210");

        when(userRepository.existsByPhoneNumber("9876543210")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void login_Success() {
        LoginRequest request = new LoginRequest("9876543210", "Secret@123");

        User user = new User(1L, "9876543210", null, "hashedPassword", "Rahul Kumar", UserRole.ROLE_STUDENT);
        user.setId(10L);

        when(userRepository.findByPhoneNumber("9876543210")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret@123", "hashedPassword")).thenReturn(true);
        when(tokenProvider.generateToken(any(UserPrincipal.class))).thenReturn("jwt.valid.token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        TokenResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt.valid.token", response.getAccessToken());
        assertEquals(10L, response.getUser().getId());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when password does not match")
    void login_InvalidPassword_ThrowsUnauthorized() {
        LoginRequest request = new LoginRequest("9876543210", "WrongPassword");

        User user = new User(1L, "9876543210", null, "hashedPassword", "Rahul Kumar", UserRole.ROLE_STUDENT);

        when(userRepository.findByPhoneNumber("9876543210")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "hashedPassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }
}
