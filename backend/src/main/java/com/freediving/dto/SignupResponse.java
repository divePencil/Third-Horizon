package com.freediving.dto;

import com.freediving.domain.Signup;

public record SignupResponse(Signup signup, String tempGroupQrUrl) {
}
