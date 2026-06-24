package com.freediving.dto;

import com.freediving.domain.ActivityStatus;
import com.freediving.domain.Visibility;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record ActivityRequest(
        @NotBlank String title,
        String location,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer capacity,
        Integer feeCents,
        Boolean aa,
        String feeDescription,
        String tempGroupQrUrl,
        ActivityStatus status,
        Visibility visibility,
        String coverUrl,
        String summary,
        String requirements,
        String safetyNotes,
        String activityContents,
        String joinConditions,
        String equipmentItems,
        String meetingLocation,
        LocalDateTime meetingTime,
        String meetingMapUrl,
        String itinerary,
        String destinationName,
        String destinationMapUrl,
        String destinationFacilities,
        Boolean disclaimerRequired
) {
}
