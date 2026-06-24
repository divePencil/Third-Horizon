package com.freediving.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinGroupRequest(
        @NotBlank String name,
        String description,
        String qrUrl,
        Integer sortOrder,
        Boolean visible
) {
}
