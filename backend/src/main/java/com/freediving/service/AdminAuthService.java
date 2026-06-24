package com.freediving.service;

import com.freediving.dto.AdminLoginRequest;
import com.freediving.dto.AdminLoginResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminAuthService {

    private final String username;
    private final String password;
    private final String tokenSecret;
    private final long tokenTtlHours;

    public AdminAuthService(
            @Value("${app.admin.username}") String username,
            @Value("${app.admin.password}") String password,
            @Value("${app.admin.token-secret}") String tokenSecret,
            @Value("${app.admin.token-ttl-hours}") long tokenTtlHours
    ) {
        this.username = username;
        this.password = password;
        this.tokenSecret = tokenSecret;
        this.tokenTtlHours = tokenTtlHours;
    }

    public AdminLoginResponse login(AdminLoginRequest request) {
        if (!constantTimeEquals(username, request.username()) || !constantTimeEquals(password, request.password())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        long expiresAt = Instant.now().plus(Duration.ofHours(tokenTtlHours)).toEpochMilli();
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String payload = username + "." + expiresAt + "." + nonce;
        String signature = sign(payload);
        String token = encode(username) + "." + expiresAt + "." + nonce + "." + signature;
        return new AdminLoginResponse(token, expiresAt, username);
    }

    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        String[] parts = token.split("\\.");
        if (parts.length != 4) {
            return false;
        }

        String tokenUsername;
        long expiresAt;
        try {
            tokenUsername = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            expiresAt = Long.parseLong(parts[1]);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        if (!constantTimeEquals(username, tokenUsername) || expiresAt < Instant.now().toEpochMilli()) {
            return false;
        }

        String payload = tokenUsername + "." + parts[1] + "." + parts[2];
        return constantTimeEquals(sign(payload), parts[3]);
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(tokenSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("failed to sign admin token", ex);
        }
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }
}
