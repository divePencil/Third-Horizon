package com.freediving.dto;

public record UserSignupRequest(
        String nickname,
        String wechatId,
        String phone,
        String emergencyContact,
        String emergencyPhone,
        String experienceLevel,
        Boolean hasInsurance,
        String note
) {
}
