package com.freediving.dto;

import com.freediving.domain.Visibility;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record AlbumRequest(
        Long activityId,
        @NotBlank String title,
        String location,
        LocalDate activityDate,
        String coverUrl,
        String story,
        Visibility visibility
) {
}
