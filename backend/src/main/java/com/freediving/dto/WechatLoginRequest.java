package com.freediving.dto;

import jakarta.validation.constraints.NotBlank;

public record WechatLoginRequest(
        @NotBlank String code,
        String nickname,
        String avatarUrl
) {
}
