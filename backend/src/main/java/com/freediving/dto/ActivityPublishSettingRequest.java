package com.freediving.dto;

import jakarta.validation.constraints.NotBlank;

public record ActivityPublishSettingRequest(
        @NotBlank String disclaimerContent
) {
}
