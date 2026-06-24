package com.freediving.dto;

public record AdminLoginResponse(
        String token,
        long expiresAt,
        String username
) {
}
