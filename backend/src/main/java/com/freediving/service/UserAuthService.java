package com.freediving.service;

import com.freediving.domain.AppUser;
import com.freediving.dto.UserLoginResponse;
import com.freediving.dto.WechatLoginRequest;
import com.freediving.repository.AppUserRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class UserAuthService {

    private final AppUserRepository userRepository;
    private final String tokenSecret;
    private final long tokenTtlHours;
    private final String wechatAppId;
    private final String wechatSecret;
    private final RestTemplate restTemplate = new RestTemplate();

    public UserAuthService(
            AppUserRepository userRepository,
            @Value("${app.user.token-secret:${app.admin.token-secret}}") String tokenSecret,
            @Value("${app.user.token-ttl-hours:720}") long tokenTtlHours,
            @Value("${app.wechat.app-id:}") String wechatAppId,
            @Value("${app.wechat.secret:}") String wechatSecret
    ) {
        this.userRepository = userRepository;
        this.tokenSecret = tokenSecret;
        this.tokenTtlHours = tokenTtlHours;
        this.wechatAppId = wechatAppId;
        this.wechatSecret = wechatSecret;
    }

    public UserLoginResponse login(WechatLoginRequest request) {
        WechatSession session = resolveWechatSession(request.code());
        AppUser user = userRepository.findByOpenid(session.openid())
                .orElseGet(() -> {
                    AppUser created = new AppUser();
                    created.setOpenid(session.openid());
                    return created;
                });
        if (StringUtils.hasText(session.unionid())) {
            user.setUnionid(session.unionid());
        }
        if (StringUtils.hasText(request.nickname())) {
            user.setNickname(request.nickname());
        }
        if (StringUtils.hasText(request.avatarUrl())) {
            user.setAvatarUrl(request.avatarUrl());
        }
        AppUser savedUser = userRepository.save(user);
        long expiresAt = Instant.now().plus(Duration.ofHours(tokenTtlHours)).toEpochMilli();
        return new UserLoginResponse(createToken(savedUser.getId(), expiresAt), expiresAt, savedUser);
    }

    public Long validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }

        String[] parts = token.split("\\.");
        if (parts.length != 4) {
            return null;
        }

        Long userId;
        long expiresAt;
        try {
            userId = Long.parseLong(new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8));
            expiresAt = Long.parseLong(parts[1]);
        } catch (IllegalArgumentException ex) {
            return null;
        }

        if (expiresAt < Instant.now().toEpochMilli()) {
            return null;
        }

        String payload = userId + "." + parts[1] + "." + parts[2];
        return constantTimeEquals(sign(payload), parts[3]) ? userId : null;
    }

    private String createToken(Long userId, long expiresAt) {
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String payload = userId + "." + expiresAt + "." + nonce;
        return encode(String.valueOf(userId)) + "." + expiresAt + "." + nonce + "." + sign(payload);
    }

    private WechatSession resolveWechatSession(String code) {
        if (!StringUtils.hasText(wechatAppId) || !StringUtils.hasText(wechatSecret)) {
            return new WechatSession("dev-" + code, null, null, null);
        }

        String url = UriComponentsBuilder.fromHttpUrl("https://api.weixin.qq.com/sns/jscode2session")
                .queryParam("appid", wechatAppId)
                .queryParam("secret", wechatSecret)
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .toUriString();
        WechatSession session = restTemplate.getForObject(url, WechatSession.class);
        if (session == null || !StringUtils.hasText(session.openid())) {
            throw new IllegalArgumentException("微信登录失败");
        }
        if (StringUtils.hasText(session.errmsg())) {
            throw new IllegalArgumentException(session.errmsg());
        }
        return session;
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(tokenSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("failed to sign user token", ex);
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WechatSession(String openid, String unionid, String errmsg, Integer errcode) {
    }
}
