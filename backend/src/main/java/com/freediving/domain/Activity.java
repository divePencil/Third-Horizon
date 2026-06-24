package com.freediving.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "third_horizon_activities")
public class Activity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 120)
    private String location;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer capacity;

    private Integer feeCents;

    @Column(nullable = false)
    private boolean aa = true;

    @Column(columnDefinition = "TEXT")
    private String feeDescription;

    @Column(length = 300)
    private String tempGroupQrUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityStatus status = ActivityStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Visibility visibility = Visibility.PUBLIC;

    @Column(length = 300)
    private String coverUrl;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(columnDefinition = "TEXT")
    private String safetyNotes;

    @Column(columnDefinition = "TEXT")
    private String activityContents;

    @Column(columnDefinition = "TEXT")
    private String joinConditions;

    @Column(columnDefinition = "TEXT")
    private String equipmentItems;

    @Column(length = 120)
    private String meetingLocation;

    private LocalDateTime meetingTime;

    @Column(length = 300)
    private String meetingMapUrl;

    @Column(columnDefinition = "TEXT")
    private String itinerary;

    @Column(length = 120)
    private String destinationName;

    @Column(length = 300)
    private String destinationMapUrl;

    @Column(columnDefinition = "TEXT")
    private String destinationFacilities;

    @Column(nullable = false)
    private boolean disclaimerRequired = true;
}
