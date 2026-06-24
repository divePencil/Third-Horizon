package com.freediving.dto;

import com.freediving.domain.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MediaAssetRequest(
        @NotNull Long albumId,
        @NotBlank String url,
        @NotBlank String objectKey,
        String title,
        String caption,
        MediaType type,
        Integer sortOrder
) {
}
