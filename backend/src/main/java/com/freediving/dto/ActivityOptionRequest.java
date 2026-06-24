package com.freediving.dto;

import com.freediving.domain.ActivityOptionCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ActivityOptionRequest(
        @NotNull ActivityOptionCategory category,
        String activityType,
        @NotBlank String label,
        Integer sortOrder,
        Boolean visible
) {
}
