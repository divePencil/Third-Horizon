package com.freediving.controller;

import com.freediving.domain.Activity;
import com.freediving.domain.Album;
import com.freediving.domain.Article;
import com.freediving.domain.MediaAsset;
import com.freediving.domain.Signup;
import com.freediving.dto.ActivityRequest;
import com.freediving.dto.ActivityOptionRequest;
import com.freediving.dto.ActivityPublishConfigResponse;
import com.freediving.dto.ActivityPublishSettingRequest;
import com.freediving.dto.AlbumRequest;
import com.freediving.dto.ArticleRequest;
import com.freediving.dto.JoinGroupRequest;
import com.freediving.dto.JoinInfoResponse;
import com.freediving.dto.JoinSettingRequest;
import com.freediving.dto.MediaAssetRequest;
import com.freediving.dto.UploadResponse;
import com.freediving.repository.ActivityRepository;
import com.freediving.repository.AlbumRepository;
import com.freediving.repository.ArticleRepository;
import com.freediving.repository.MediaAssetRepository;
import com.freediving.repository.SignupRepository;
import com.freediving.service.CosStorageService;
import com.freediving.service.JoinInfoService;
import com.freediving.service.ActivityPublishConfigService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ActivityRepository activityRepository;
    private final AlbumRepository albumRepository;
    private final ArticleRepository articleRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final SignupRepository signupRepository;
    private final CosStorageService cosStorageService;
    private final JoinInfoService joinInfoService;
    private final ActivityPublishConfigService activityPublishConfigService;

    public AdminController(
            ActivityRepository activityRepository,
            AlbumRepository albumRepository,
            ArticleRepository articleRepository,
            MediaAssetRepository mediaAssetRepository,
            SignupRepository signupRepository,
            CosStorageService cosStorageService,
            JoinInfoService joinInfoService,
            ActivityPublishConfigService activityPublishConfigService
    ) {
        this.activityRepository = activityRepository;
        this.albumRepository = albumRepository;
        this.articleRepository = articleRepository;
        this.mediaAssetRepository = mediaAssetRepository;
        this.signupRepository = signupRepository;
        this.cosStorageService = cosStorageService;
        this.joinInfoService = joinInfoService;
        this.activityPublishConfigService = activityPublishConfigService;
    }

    @GetMapping("/activities")
    public Page<Activity> activities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return activityRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startTime", "createdAt")));
    }

    @PostMapping("/activities")
    public Activity createActivity(@Valid @RequestBody ActivityRequest request) {
        return activityRepository.save(apply(new Activity(), request));
    }

    @PutMapping("/activities/{id}")
    public Activity updateActivity(@PathVariable Long id, @Valid @RequestBody ActivityRequest request) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("activity not found"));
        return activityRepository.save(apply(activity, request));
    }

    @DeleteMapping("/activities/{id}")
    @Transactional
    public void deleteActivity(@PathVariable Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("activity not found"));
        List<Album> albums = albumRepository.findByActivityId(id);
        albums.forEach(album -> mediaAssetRepository.deleteByAlbumId(album.getId()));
        albumRepository.deleteAll(albums);
        signupRepository.deleteByActivityId(id);
        activityRepository.delete(activity);
    }

    @GetMapping("/albums")
    public Page<Album> albums(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return albumRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "activityDate", "createdAt")));
    }

    @PostMapping("/albums")
    public Album createAlbum(@Valid @RequestBody AlbumRequest request) {
        return albumRepository.save(apply(new Album(), request));
    }

    @PutMapping("/albums/{id}")
    public Album updateAlbum(@PathVariable Long id, @Valid @RequestBody AlbumRequest request) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("album not found"));
        return albumRepository.save(apply(album, request));
    }

    @GetMapping("/albums/{id}/media")
    public List<MediaAsset> albumMedia(@PathVariable Long id) {
        albumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("album not found"));
        return mediaAssetRepository.findByAlbumIdOrderBySortOrderAscIdAsc(id);
    }

    @DeleteMapping("/albums/{id}")
    @Transactional
    public void deleteAlbum(@PathVariable Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("album not found"));
        mediaAssetRepository.deleteByAlbumId(id);
        albumRepository.delete(album);
    }

    @PostMapping("/media")
    public MediaAsset createMedia(@Valid @RequestBody MediaAssetRequest request) {
        MediaAsset media = new MediaAsset();
        media.setAlbumId(request.albumId());
        media.setUrl(request.url());
        media.setObjectKey(request.objectKey());
        media.setTitle(request.title());
        media.setCaption(request.caption());
        media.setType(request.type() == null ? media.getType() : request.type());
        media.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        return mediaAssetRepository.save(media);
    }

    @DeleteMapping("/media/{id}")
    public void deleteMedia(@PathVariable Long id) {
        MediaAsset media = mediaAssetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("media not found"));
        mediaAssetRepository.delete(media);
    }

    @GetMapping("/articles")
    public Page<Article> articles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return articleRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt")));
    }

    @PostMapping("/articles")
    public Article createArticle(@Valid @RequestBody ArticleRequest request) {
        return articleRepository.save(apply(new Article(), request));
    }

    @PutMapping("/articles/{id}")
    public Article updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleRequest request) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("article not found"));
        return articleRepository.save(apply(article, request));
    }

    @DeleteMapping("/articles/{id}")
    public void deleteArticle(@PathVariable Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("article not found"));
        articleRepository.delete(article);
    }

    @GetMapping("/signups")
    public List<Signup> signups(@RequestParam(required = false) Long activityId) {
        if (activityId == null) {
            return signupRepository.findAll();
        }
        return signupRepository.findByActivityIdOrderByCreatedAtAsc(activityId);
    }

    @GetMapping("/join-info")
    public JoinInfoResponse joinInfo() {
        return joinInfoService.adminInfo();
    }

    @PutMapping("/join-info/setting")
    public Object updateJoinSetting(@Valid @RequestBody JoinSettingRequest request) {
        return joinInfoService.updateSetting(request);
    }

    @PostMapping("/join-info/groups")
    public Object createJoinGroup(@Valid @RequestBody JoinGroupRequest request) {
        return joinInfoService.createGroup(request);
    }

    @PutMapping("/join-info/groups/{id}")
    public Object updateJoinGroup(@PathVariable Long id, @Valid @RequestBody JoinGroupRequest request) {
        return joinInfoService.updateGroup(id, request);
    }

    @DeleteMapping("/join-info/groups/{id}")
    public void deleteJoinGroup(@PathVariable Long id) {
        joinInfoService.deleteGroup(id);
    }

    @GetMapping("/activity-publish-config")
    public ActivityPublishConfigResponse activityPublishConfig() {
        return activityPublishConfigService.adminConfig();
    }

    @PutMapping("/activity-publish-config/setting")
    public Object updateActivityPublishSetting(@Valid @RequestBody ActivityPublishSettingRequest request) {
        return activityPublishConfigService.updateSetting(request);
    }

    @PostMapping("/activity-publish-config/options")
    public Object createActivityOption(@Valid @RequestBody ActivityOptionRequest request) {
        return activityPublishConfigService.createOption(request);
    }

    @PutMapping("/activity-publish-config/options/{id}")
    public Object updateActivityOption(@PathVariable Long id, @Valid @RequestBody ActivityOptionRequest request) {
        return activityPublishConfigService.updateOption(id, request);
    }

    @DeleteMapping("/activity-publish-config/options/{id}")
    public void deleteActivityOption(@PathVariable Long id) {
        activityPublishConfigService.deleteOption(id);
    }

    @PostMapping("/uploads")
    public UploadResponse upload(@RequestParam MultipartFile file, @RequestParam(defaultValue = "albums") String folder) throws IOException {
        return cosStorageService.upload(file, folder);
    }

    private Activity apply(Activity activity, ActivityRequest request) {
        activity.setTitle(request.title());
        activity.setLocation(request.location());
        activity.setStartTime(request.startTime());
        activity.setEndTime(request.endTime());
        activity.setCapacity(request.capacity());
        activity.setFeeCents(request.feeCents());
        activity.setAa(request.aa() == null || request.aa());
        activity.setFeeDescription(request.feeDescription());
        activity.setTempGroupQrUrl(request.tempGroupQrUrl());
        activity.setStatus(request.status() == null ? activity.getStatus() : request.status());
        activity.setVisibility(request.visibility() == null ? activity.getVisibility() : request.visibility());
        activity.setCoverUrl(request.coverUrl());
        activity.setSummary(request.summary());
        activity.setRequirements(request.requirements());
        activity.setSafetyNotes(request.safetyNotes());
        activity.setActivityContents(request.activityContents());
        activity.setJoinConditions(request.joinConditions());
        activity.setEquipmentItems(request.equipmentItems());
        activity.setMeetingLocation(request.meetingLocation());
        activity.setMeetingTime(request.meetingTime());
        activity.setMeetingMapUrl(request.meetingMapUrl());
        activity.setItinerary(request.itinerary());
        activity.setDestinationName(request.destinationName());
        activity.setDestinationMapUrl(request.destinationMapUrl());
        activity.setDestinationFacilities(request.destinationFacilities());
        activity.setDisclaimerRequired(request.disclaimerRequired() == null || request.disclaimerRequired());
        return activity;
    }

    private Album apply(Album album, AlbumRequest request) {
        album.setActivityId(request.activityId());
        album.setTitle(request.title());
        album.setLocation(request.location());
        album.setActivityDate(request.activityDate());
        album.setCoverUrl(request.coverUrl());
        album.setStory(request.story());
        album.setVisibility(request.visibility() == null ? album.getVisibility() : request.visibility());
        return album;
    }

    private Article apply(Article article, ArticleRequest request) {
        article.setTitle(request.title());
        if (article.getSlug() == null || article.getSlug().isBlank()) {
            article.setSlug(generateSlug(request.title()));
        }
        article.setCategory(request.category() == null || request.category().isBlank() ? "新人指南" : request.category());
        article.setCoverUrl(request.coverUrl());
        article.setExcerpt(request.excerpt());
        article.setContent(request.content());
        article.setVisibility(request.visibility() == null ? article.getVisibility() : request.visibility());
        article.setPublished(request.published() == null || request.published());
        return article;
    }

    private String generateSlug(String title) {
        String slug = title == null ? "article" : title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (slug.isBlank()) {
            slug = "article";
        }
        return slug + "-" + Instant.now().toEpochMilli();
    }
}
