package com.freediving.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SignupRequest(
        @NotNull Long activityId,
        @NotBlank String nickname,
        String wechatId,
        String phone,
        String emergencyContact,
        String emergencyPhone,
        String experienceLevel,
        Boolean hasInsurance,
        String note
) {
}
