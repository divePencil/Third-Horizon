package com.freediving.controller;

import com.freediving.domain.Activity;
import com.freediving.domain.ActivityExpense;
import com.freediving.domain.ActivityExpenseShare;
import com.freediving.domain.ActivityMember;
import com.freediving.domain.ActivityMemberRole;
import com.freediving.domain.ActivityMemberStatus;
import com.freediving.domain.ActivityStatus;
import com.freediving.domain.Album;
import com.freediving.domain.AppUser;
import com.freediving.domain.MediaAsset;
import com.freediving.domain.Signup;
import com.freediving.domain.Visibility;
import com.freediving.dto.ActivityExpenseRequest;
import com.freediving.dto.ActivityExpenseResponse;
import com.freediving.dto.ActivityRequest;
import com.freediving.dto.AlbumRequest;
import com.freediving.dto.MediaAssetRequest;
import com.freediving.dto.SettlementResponse;
import com.freediving.dto.SignupResponse;
import com.freediving.dto.UploadResponse;
import com.freediving.dto.UserActivityResponse;
import com.freediving.dto.UserProfileRequest;
import com.freediving.dto.UserSignupRequest;
import com.freediving.repository.ActivityExpenseRepository;
import com.freediving.repository.ActivityExpenseShareRepository;
import com.freediving.repository.ActivityMemberRepository;
import com.freediving.repository.ActivityRepository;
import com.freediving.repository.AlbumRepository;
import com.freediving.repository.AppUserRepository;
import com.freediving.repository.MediaAssetRepository;
import com.freediving.repository.SignupRepository;
import com.freediving.service.CosStorageService;
import com.freediving.service.SettlementService;
import com.freediving.web.UserContext;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AppUserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final ActivityMemberRepository memberRepository;
    private final SignupRepository signupRepository;
    private final ActivityExpenseRepository expenseRepository;
    private final ActivityExpenseShareRepository shareRepository;
    private final AlbumRepository albumRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final CosStorageService cosStorageService;
    private final SettlementService settlementService;

    public UserController(
            AppUserRepository userRepository,
            ActivityRepository activityRepository,
            ActivityMemberRepository memberRepository,
            SignupRepository signupRepository,
            ActivityExpenseRepository expenseRepository,
            ActivityExpenseShareRepository shareRepository,
            AlbumRepository albumRepository,
            MediaAssetRepository mediaAssetRepository,
            CosStorageService cosStorageService,
            SettlementService settlementService
    ) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.memberRepository = memberRepository;
        this.signupRepository = signupRepository;
        this.expenseRepository = expenseRepository;
        this.shareRepository = shareRepository;
        this.albumRepository = albumRepository;
        this.mediaAssetRepository = mediaAssetRepository;
        this.cosStorageService = cosStorageService;
        this.settlementService = settlementService;
    }

    @GetMapping("/me")
    public AppUser me() {
        return currentUser();
    }

    @PutMapping("/me")
    public AppUser updateMe(@RequestBody UserProfileRequest request) {
        AppUser user = currentUser();
        user.setNickname(request.nickname());
        user.setAvatarUrl(request.avatarUrl());
        user.setPhone(request.phone());
        return userRepository.save(user);
    }

    @GetMapping("/activities")
    public List<UserActivityResponse> myActivities() {
        return memberRepository.findByUserIdAndStatusOrderByCreatedAtDesc(currentUserId(), ActivityMemberStatus.JOINED)
                .stream()
                .map(member -> new UserActivityResponse(
                        activityRepository.findById(member.getActivityId()).orElse(null),
                        member
                ))
                .filter(response -> response.activity() != null)
                .toList();
    }

    @PostMapping("/activities")
    public Activity createActivity(@Valid @RequestBody ActivityRequest request) {
        Activity activity = activityRepository.save(apply(new Activity(), request));
        ActivityMember owner = new ActivityMember();
        owner.setActivityId(activity.getId());
        owner.setUserId(currentUserId());
        owner.setRole(ActivityMemberRole.OWNER);
        memberRepository.save(owner);
        return activity;
    }

    @PutMapping("/activities/{id}")
    public Activity updateActivity(@PathVariable Long id, @Valid @RequestBody ActivityRequest request) {
        ensureOwner(id);
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("activity not found"));
        return activityRepository.save(apply(activity, request));
    }

    @PostMapping("/activities/{id}/signup")
    @Transactional
    public SignupResponse signup(@PathVariable Long id, @RequestBody UserSignupRequest request) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("activity not found"));
        AppUser user = currentUser();
        Signup signup = new Signup();
        signup.setActivityId(id);
        signup.setNickname(firstText(request.nickname(), user.getNickname(), "微信用户"));
        signup.setWechatId(request.wechatId());
        signup.setPhone(firstText(request.phone(), user.getPhone()));
        signup.setEmergencyContact(request.emergencyContact());
        signup.setEmergencyPhone(request.emergencyPhone());
        signup.setExperienceLevel(request.experienceLevel());
        signup.setHasInsurance(Boolean.TRUE.equals(request.hasInsurance()));
        signup.setNote(request.note());
        Signup savedSignup = signupRepository.save(signup);

        ActivityMember member = memberRepository.findByActivityIdAndUserId(id, user.getId())
                .orElseGet(ActivityMember::new);
        member.setActivityId(id);
        member.setUserId(user.getId());
        member.setSignupId(savedSignup.getId());
        member.setRole(member.getRole() == null ? ActivityMemberRole.MEMBER : member.getRole());
        member.setStatus(ActivityMemberStatus.JOINED);
        memberRepository.save(member);
        return new SignupResponse(savedSignup, activity.getTempGroupQrUrl());
    }

    @GetMapping("/activities/{id}/members")
    public List<ActivityMember> members(@PathVariable Long id) {
        ensureMember(id);
        return memberRepository.findByActivityIdAndStatusOrderByCreatedAtAsc(id, ActivityMemberStatus.JOINED);
    }

    @GetMapping("/activities/{id}/expenses")
    public List<ActivityExpenseResponse> expenses(@PathVariable Long id) {
        ensureMember(id);
        return expenseRepository.findByActivityIdOrderByCreatedAtAsc(id).stream()
                .map(expense -> new ActivityExpenseResponse(expense, shareRepository.findByExpenseId(expense.getId())))
                .toList();
    }

    @PostMapping("/activities/{id}/expenses")
    @Transactional
    public ActivityExpenseResponse createExpense(@PathVariable Long id, @Valid @RequestBody ActivityExpenseRequest request) {
        ensureMember(id);
        ActivityExpense expense = new ActivityExpense();
        expense.setActivityId(id);
        expense.setPayerUserId(currentUserId());
        expense.setTitle(request.title());
        expense.setAmountCents(request.amountCents());
        expense.setNote(request.note());
        expense.setReceiptUrl(request.receiptUrl());
        ActivityExpense savedExpense = expenseRepository.save(expense);

        int shareCount = request.shareUserIds().size();
        int base = request.amountCents() / shareCount;
        int remainder = request.amountCents() % shareCount;
        List<ActivityExpenseShare> shares = request.shareUserIds().stream().map(userId -> {
            ensureMember(id, userId);
            ActivityExpenseShare share = new ActivityExpenseShare();
            share.setExpenseId(savedExpense.getId());
            share.setUserId(userId);
            return share;
        }).toList();
        for (int i = 0; i < shares.size(); i++) {
            shares.get(i).setShareCents(base + (i < remainder ? 1 : 0));
        }
        return new ActivityExpenseResponse(savedExpense, shareRepository.saveAll(shares));
    }

    @PostMapping("/activities/{id}/settlement")
    public SettlementResponse generateSettlement(@PathVariable Long id) {
        return settlementService.generate(id, currentUserId());
    }

    @GetMapping("/activities/{id}/settlement")
    public SettlementResponse latestSettlement(@PathVariable Long id) {
        ensureMember(id);
        return settlementService.latest(id);
    }

    @PostMapping("/activities/{id}/albums")
    public Album createActivityAlbum(@PathVariable Long id, @Valid @RequestBody AlbumRequest request) {
        ensureMember(id);
        Album album = new Album();
        album.setActivityId(id);
        album.setTitle(request.title());
        album.setLocation(request.location());
        album.setActivityDate(request.activityDate());
        album.setCoverUrl(request.coverUrl());
        album.setStory(request.story());
        album.setVisibility(request.visibility() == null ? Visibility.PUBLIC : request.visibility());
        return albumRepository.save(album);
    }

    @PostMapping("/albums/{id}/media")
    public MediaAsset createMedia(@PathVariable Long id, @Valid @RequestBody MediaAssetRequest request) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("album not found"));
        ensureMember(album.getActivityId());
        MediaAsset media = new MediaAsset();
        media.setAlbumId(id);
        media.setUrl(request.url());
        media.setObjectKey(request.objectKey());
        media.setTitle(request.title());
        media.setCaption(request.caption());
        media.setType(request.type() == null ? media.getType() : request.type());
        media.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        MediaAsset savedMedia = mediaAssetRepository.save(media);
        if (album.getCoverUrl() == null || album.getCoverUrl().isBlank()) {
            album.setCoverUrl(savedMedia.getUrl());
            albumRepository.save(album);
        }
        return savedMedia;
    }

    @PostMapping("/uploads")
    public UploadResponse upload(@RequestParam MultipartFile file, @RequestParam(defaultValue = "miniapp") String folder) throws IOException {
        return cosStorageService.upload(file, folder);
    }

    private AppUser currentUser() {
        return userRepository.findById(currentUserId())
                .orElseThrow(() -> new EntityNotFoundException("user not found"));
    }

    private Long currentUserId() {
        Long userId = UserContext.userId();
        if (userId == null) {
            throw new IllegalStateException("user not authenticated");
        }
        return userId;
    }

    private void ensureOwner(Long activityId) {
        ActivityMember member = ensureMember(activityId);
        if (member.getRole() != ActivityMemberRole.OWNER) {
            throw new IllegalArgumentException("只有活动发起人可以操作");
        }
    }

    private ActivityMember ensureMember(Long activityId) {
        return ensureMember(activityId, currentUserId());
    }

    private ActivityMember ensureMember(Long activityId, Long userId) {
        return memberRepository.findByActivityIdAndUserId(activityId, userId)
                .filter(member -> member.getStatus() == ActivityMemberStatus.JOINED)
                .orElseThrow(() -> new EntityNotFoundException("activity member not found"));
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
        activity.setStatus(request.status() == null ? ActivityStatus.OPEN : request.status());
        activity.setVisibility(request.visibility() == null ? Visibility.PUBLIC : request.visibility());
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

    private String firstText(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
