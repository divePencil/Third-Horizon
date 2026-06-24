package com.freediving.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinSettingRequest(
        @NotBlank String title,
        @NotBlank String subtitle,
        @NotBlank String managerName,
        @NotBlank String managerWechatId,
        String managerNote
) {
}
