package com.freediving.dto;

import com.freediving.domain.Visibility;
import jakarta.validation.constraints.NotBlank;

public record ArticleRequest(
        @NotBlank String title,
        String category,
        String coverUrl,
        String excerpt,
        @NotBlank String content,
        Visibility visibility,
        Boolean published
) {
}
