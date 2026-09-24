package com.digitallibrary.modules.auth;

import com.digitallibrary.core.errors.BadRequestException;
import com.digitallibrary.core.errors.ConflictException;
import com.digitallibrary.core.errors.ResourceNotFoundException;
import com.digitallibrary.core.errors.UnauthorizedException;
import com.digitallibrary.core.security.JwtTokenProvider;
import com.digitallibrary.core.security.UserPrincipal;
import com.digitallibrary.modules.auth.dto.LoginRequest;
import com.digitallibrary.modules.auth.dto.RefreshTokenRequest;
import com.digitallibrary.modules.auth.dto.RegisterRequest;
import com.digitallibrary.modules.auth.dto.TokenResponse;
import com.digitallibrary.modules.user.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final long jwtExpirationMs;
    private final long refreshExpirationMs;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider,
            @Value("${app.jwt.expiration-ms:3600000}") long jwtExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms:2592000000}") long refreshExpirationMs) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.jwtExpirationMs = jwtExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ConflictException("User already exists with phone number: " + request.getPhoneNumber(), "PHONE_ALREADY_EXISTS");
        }

        User user = new User(
                request.getLibraryId(),
                request.getPhoneNumber(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName(),
                UserRole.ROLE_STUDENT
        );

        user = userRepository.save(user);

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String accessToken = tokenProvider.generateToken(userPrincipal);
        RefreshToken refreshToken = createRefreshToken(user, "Mobile Device");

        return new TokenResponse(accessToken, refreshToken.getToken(), jwtExpirationMs, UserDto.fromEntity(user));
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new UnauthorizedException("Invalid phone number or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid phone number or password");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new BadRequestException("Account has been suspended by library administration. Please contact the owner.", "ACCOUNT_SUSPENDED");
        }

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String accessToken = tokenProvider.generateToken(userPrincipal);
        RefreshToken refreshToken = createRefreshToken(user, "Mobile Device");

        return new TokenResponse(accessToken, refreshToken.getToken(), jwtExpirationMs, UserDto.fromEntity(user));
    }

    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException("Refresh token has expired. Please login again.");
        }

        User user = refreshToken.getUser();
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new BadRequestException("Account is suspended.", "ACCOUNT_SUSPENDED");
        }

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String newAccessToken = tokenProvider.generateToken(userPrincipal);

        return new TokenResponse(newAccessToken, refreshToken.getToken(), jwtExpirationMs, UserDto.fromEntity(user));
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return UserDto.fromEntity(user);
    }

    @Transactional
    public void logout(Long userId) {
        userRepository.findById(userId).ifPresent(refreshTokenRepository::deleteByUser);
    }

    private RefreshToken createRefreshToken(User user, String deviceInfo) {
        // Invalidate old tokens for this user
        refreshTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plusMillis(refreshExpirationMs);

        RefreshToken refreshToken = new RefreshToken(user, token, deviceInfo, expiresAt);
        return refreshTokenRepository.save(refreshToken);
    }
}
