package com.freediving.service;

import com.freediving.domain.ActivityOption;
import com.freediving.domain.ActivityPublishSetting;
import com.freediving.dto.ActivityOptionRequest;
import com.freediving.dto.ActivityPublishConfigResponse;
import com.freediving.dto.ActivityPublishSettingRequest;
import com.freediving.repository.ActivityOptionRepository;
import com.freediving.repository.ActivityPublishSettingRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ActivityPublishConfigService {

    private static final long SETTING_ID = 1L;

    private final ActivityPublishSettingRepository settingRepository;
    private final ActivityOptionRepository optionRepository;

    public ActivityPublishConfigService(
            ActivityPublishSettingRepository settingRepository,
            ActivityOptionRepository optionRepository
    ) {
        this.settingRepository = settingRepository;
        this.optionRepository = optionRepository;
    }

    public ActivityPublishConfigResponse publicConfig() {
        return new ActivityPublishConfigResponse(setting(), optionRepository.findByVisibleTrueOrderByCategoryAscActivityTypeAscSortOrderAscIdAsc());
    }

    public ActivityPublishConfigResponse adminConfig() {
        return new ActivityPublishConfigResponse(setting(), optionRepository.findAllByOrderByCategoryAscActivityTypeAscSortOrderAscIdAsc());
    }

    @Transactional
    public ActivityPublishSetting updateSetting(ActivityPublishSettingRequest request) {
        ActivityPublishSetting setting = setting();
        setting.setDisclaimerContent(request.disclaimerContent());
        return settingRepository.save(setting);
    }

    @Transactional
    public ActivityOption createOption(ActivityOptionRequest request) {
        return optionRepository.save(apply(new ActivityOption(), request));
    }

    @Transactional
    public ActivityOption updateOption(Long id, ActivityOptionRequest request) {
        ActivityOption option = optionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("activity option not found"));
        return optionRepository.save(apply(option, request));
    }

    public void deleteOption(Long id) {
        ActivityOption option = optionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("activity option not found"));
        optionRepository.delete(option);
    }

    private ActivityPublishSetting setting() {
        return settingRepository.findById(SETTING_ID).orElseGet(() -> {
            ActivityPublishSetting setting = new ActivityPublishSetting();
            setting.setId(SETTING_ID);
            return settingRepository.save(setting);
        });
    }

    private ActivityOption apply(ActivityOption option, ActivityOptionRequest request) {
        option.setCategory(request.category());
        option.setActivityType(request.activityType());
        option.setLabel(request.label());
        option.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        option.setVisible(request.visible() == null || request.visible());
        return option;
    }
}
