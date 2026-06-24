package com.freediving.dto;

import com.freediving.domain.AppUser;

public record UserLoginResponse(String token, Long expiresAt, AppUser user) {
}
