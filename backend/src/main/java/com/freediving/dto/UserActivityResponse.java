package com.freediving.dto;

import com.freediving.domain.Activity;
import com.freediving.domain.ActivityMember;

public record UserActivityResponse(Activity activity, ActivityMember member) {
}
