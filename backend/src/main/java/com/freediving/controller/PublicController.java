package com.freediving.controller;

import com.freediving.domain.Activity;
import com.freediving.domain.ActivityStatus;
import com.freediving.domain.Album;
import com.freediving.domain.Article;
import com.freediving.domain.MediaAsset;
import com.freediving.domain.Signup;
import com.freediving.domain.Visibility;
import com.freediving.dto.ActivityPublishConfigResponse;
import com.freediving.dto.JoinInfoResponse;
import com.freediving.dto.SignupRequest;
import com.freediving.dto.SignupResponse;
import com.freediving.repository.ActivityRepository;
import com.freediving.repository.AlbumRepository;
import com.freediving.repository.ArticleRepository;
import com.freediving.repository.MediaAssetRepository;
import com.freediving.repository.SignupRepository;
import com.freediving.service.ActivityPublishConfigService;
import com.freediving.service.JoinInfoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final ActivityRepository activityRepository;
    private final AlbumRepository albumRepository;
    private final ArticleRepository articleRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final SignupRepository signupRepository;
    private final JoinInfoService joinInfoService;
    private final ActivityPublishConfigService activityPublishConfigService;

    public PublicController(
            ActivityRepository activityRepository,
            AlbumRepository albumRepository,
            ArticleRepository articleRepository,
            MediaAssetRepository mediaAssetRepository,
            SignupRepository signupRepository,
            JoinInfoService joinInfoService,
            ActivityPublishConfigService activityPublishConfigService
    ) {
        this.activityRepository = activityRepository;
        this.albumRepository = albumRepository;
        this.articleRepository = articleRepository;
        this.mediaAssetRepository = mediaAssetRepository;
        this.signupRepository = signupRepository;
        this.joinInfoService = joinInfoService;
        this.activityPublishConfigService = activityPublishConfigService;
    }

    @GetMapping("/activities")
    public List<Activity> activities() {
        return activityRepository.findByVisibilityAndStatusInOrderByStartTimeAsc(
                Visibility.PUBLIC,
                List.of(ActivityStatus.OPEN, ActivityStatus.FULL, ActivityStatus.FINISHED)
        ).stream().map(this::withoutTempGroupQr).toList();
    }

    @GetMapping("/activities/{id}")
    public Activity activity(@PathVariable Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("activity not found"));
        if (activity.getVisibility() != Visibility.PUBLIC) {
            throw new EntityNotFoundException("activity not found");
        }
        return withoutTempGroupQr(activity);
    }

    @PostMapping("/signups")
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
        Activity activity = activityRepository.findById(request.activityId())
                .orElseThrow(() -> new EntityNotFoundException("activity not found"));
        Signup signup = new Signup();
        signup.setActivityId(request.activityId());
        signup.setNickname(request.nickname());
        signup.setWechatId(request.wechatId());
        signup.setPhone(request.phone());
        signup.setEmergencyContact(request.emergencyContact());
        signup.setEmergencyPhone(request.emergencyPhone());
        signup.setExperienceLevel(request.experienceLevel());
        signup.setHasInsurance(Boolean.TRUE.equals(request.hasInsurance()));
        signup.setNote(request.note());
        Signup savedSignup = signupRepository.save(signup);
        return new SignupResponse(savedSignup, activity.getTempGroupQrUrl());
    }

    private Activity withoutTempGroupQr(Activity source) {
        Activity activity = new Activity();
        activity.setId(source.getId());
        activity.setTitle(source.getTitle());
        activity.setLocation(source.getLocation());
        activity.setStartTime(source.getStartTime());
        activity.setEndTime(source.getEndTime());
        activity.setCapacity(source.getCapacity());
        activity.setFeeCents(source.getFeeCents());
        activity.setAa(source.isAa());
        activity.setFeeDescription(source.getFeeDescription());
        activity.setTempGroupQrUrl(null);
        activity.setStatus(source.getStatus());
        activity.setVisibility(source.getVisibility());
        activity.setCoverUrl(source.getCoverUrl());
        activity.setSummary(source.getSummary());
        activity.setRequirements(source.getRequirements());
        activity.setSafetyNotes(source.getSafetyNotes());
        activity.setActivityContents(source.getActivityContents());
        activity.setJoinConditions(source.getJoinConditions());
        activity.setEquipmentItems(source.getEquipmentItems());
        activity.setMeetingLocation(source.getMeetingLocation());
        activity.setMeetingTime(source.getMeetingTime());
        activity.setMeetingMapUrl(source.getMeetingMapUrl());
        activity.setItinerary(source.getItinerary());
        activity.setDestinationName(source.getDestinationName());
        activity.setDestinationMapUrl(source.getDestinationMapUrl());
        activity.setDestinationFacilities(source.getDestinationFacilities());
        activity.setDisclaimerRequired(source.isDisclaimerRequired());
        return activity;
    }

    @GetMapping("/albums")
    public List<Album> albums() {
        return albumRepository.findByVisibilityOrderByActivityDateDesc(Visibility.PUBLIC);
    }

    @GetMapping("/albums/{id}")
    public Album album(@PathVariable Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("album not found"));
        if (album.getVisibility() != Visibility.PUBLIC) {
            throw new EntityNotFoundException("album not found");
        }
        return album;
    }

    @GetMapping("/albums/{id}/media")
    public List<MediaAsset> media(@PathVariable Long id) {
        Album album = album(id);
        return mediaAssetRepository.findByAlbumIdOrderBySortOrderAscIdAsc(album.getId());
    }

    @GetMapping("/articles")
    public List<Article> articles() {
        return articleRepository.findByVisibilityAndPublishedTrueOrderByUpdatedAtDesc(Visibility.PUBLIC);
    }

    @GetMapping("/join-info")
    public JoinInfoResponse joinInfo() {
        return joinInfoService.publicInfo();
    }

    @GetMapping("/activity-publish-config")
    public ActivityPublishConfigResponse activityPublishConfig() {
        return activityPublishConfigService.publicConfig();
    }

    @GetMapping("/articles/{slug}")
    public Article article(@PathVariable String slug) {
        Article article = articleRepository.findBySlugAndPublishedTrue(slug)
                .orElseThrow(() -> new EntityNotFoundException("article not found"));
        if (article.getVisibility() != Visibility.PUBLIC) {
            throw new EntityNotFoundException("article not found");
        }
        return article;
    }
}
