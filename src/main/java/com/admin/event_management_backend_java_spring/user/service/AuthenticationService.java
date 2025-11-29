package com.admin.event_management_backend_java_spring.user.service;

import com.admin.event_management_backend_java_spring.exception.AppException;
import com.admin.event_management_backend_java_spring.exception.ErrorCode;
import com.admin.event_management_backend_java_spring.security.model.TokenBlacklist;
import com.admin.event_management_backend_java_spring.security.repository.TokenBlacklistRepository;
import com.admin.event_management_backend_java_spring.user.model.User;
import com.admin.event_management_backend_java_spring.user.payload.request.AuthenticationRequest;
import com.admin.event_management_backend_java_spring.user.payload.request.IntrospectRequest;
import com.admin.event_management_backend_java_spring.user.payload.request.LogoutRequest;
import com.admin.event_management_backend_java_spring.user.payload.response.AuthenticationResponse;
import com.admin.event_management_backend_java_spring.user.payload.response.IntrospectResponse;
import com.admin.event_management_backend_java_spring.user.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AuthenticationService {
    UserRepository userRepository;
    TokenBlacklistRepository tokenBlacklistRepository;
    TwoFactorAuthService twoFactorAuthService;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    // Kiểm tra token
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;
        try {
            verifyToken(token);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    // Xác thực tài khoản
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
        }

        // Kiểm tra role và logic 2FA
        boolean requires2FA = shouldRequire2FA(user);

        if (requires2FA) {
            // Nếu cần 2FA, tự động gửi OTP
            try {
                twoFactorAuthService.sendOtp(user.getEmail());
            } catch (Exception e) {
                log.error("Failed to send OTP for user: {}", user.getEmail(), e);
                throw new AppException(ErrorCode.INTERNAL_ERROR);
            }

            // Trả về response yêu cầu 2FA
            return AuthenticationResponse.builder()
                    .token(null)
                    .authenticated(false)
                    .requires2FA(true)
                    .message("2FA verification required. OTP has been sent to your email.")
                    .build();
        }

        // Nếu không cần 2FA hoặc đã xác thực 2FA, tạo token
        var token = generateToken(user);
        var role = user.getRole();
        var expiresAt = getTokenExpirationTime(token);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .requires2FA(false)
                .role(role != null ? role.name() : null)
                .departmentId(user.getDepartment() != null ? user.getDepartment().getId() : null)
                .expiresAt(expiresAt)
                .build();
    }

    // Kiểm tra xem user có cần 2FA không
    private boolean shouldRequire2FA(User user) {
        // Admin luôn cần 2FA
        if (isAdminRole(user.getRole())) {
            return true;
        }

        // Student và Organization chỉ cần 2FA nếu đã bật
        if (isStudentOrOrganizationRole(user.getRole())) {
            return user.getTwoFactorEnabled() != null && user.getTwoFactorEnabled();
        }

        // Các role khác không cần 2FA
        return false;
    }

    // Kiểm tra xem có phải admin role không
    private boolean isAdminRole(User.UserRole role) {
        return role == User.UserRole.ADMIN ||
               role == User.UserRole.FACULTY_ADMIN;
    }

    // Kiểm tra xem có phải student hoặc organization role không
    private boolean isStudentOrOrganizationRole(User.UserRole role) {
        return role == User.UserRole.STUDENT ||
               role == User.UserRole.ORGANIZER;
    }

    private Date getTokenExpirationTime(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getExpirationTime();
        } catch (ParseException e) {
            log.error("Error parsing token", e);
            throw new AppException(ErrorCode.TOKEN_PARSING_FAILED);
        }
    }

    // Logout
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        var signedToken = verifyToken(request.getToken());

        String jit = signedToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();

        TokenBlacklist invalidatedToken = new TokenBlacklist(
            jit,
            signedToken.getJWTClaimsSet().getSubject(),
            "LOGOUT",
            expiryTime.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
        );

        tokenBlacklistRepository.save(invalidatedToken);
    }

    public SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier jwsVerifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(jwsVerifier);

        if (!(verified && expiryTime.after(new Date())))
            throw new AppException(ErrorCode.TOKEN_INVALID);

        if (tokenBlacklistRepository.existsByToken(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.TOKEN_INVALID);

        return signedJWT;
    }

    // Tạo token
    private String generateToken(User user) {
        // Header
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        // Body
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("doan")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .claim("role", user.getRole() != null ? user.getRole().name() : null)
                .claim("departmentId", user.getDepartment() != null ? user.getDepartment().getId() : null)
                .claim("userId", user.getId())
                .build();

        // Payload
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Error generating token", e);
            throw new AppException(ErrorCode.TOKEN_GENERATION_FAILED);
        }
    }

    // Tạo scope
    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (user.getRole() != null) {
            stringJoiner.add(user.getRole().name());
        }
        return stringJoiner.toString();
    }

    // Xác thực sau khi 2FA thành công
    public AuthenticationResponse authenticateAfter2FA(AuthenticationRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra xem 2FA đã được xác thực chưa
        if (!user.getTwoFactorVerified()) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
        }

        var token = generateToken(user);
        var role = user.getRole();
        var expiresAt = getTokenExpirationTime(token);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .requires2FA(false)
                .role(role != null ? role.name() : null)
                .departmentId(user.getDepartment() != null ? user.getDepartment().getId() : null)
                .expiresAt(expiresAt)
                .build();
    }

    //auth/status from token
    public boolean isAuthenticated(String token) {
        try {
            verifyToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
