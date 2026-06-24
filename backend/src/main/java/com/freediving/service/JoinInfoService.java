package com.freediving.service;

import com.freediving.domain.JoinGroup;
import com.freediving.domain.JoinSetting;
import com.freediving.dto.JoinGroupRequest;
import com.freediving.dto.JoinInfoResponse;
import com.freediving.dto.JoinSettingRequest;
import com.freediving.repository.JoinGroupRepository;
import com.freediving.repository.JoinSettingRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JoinInfoService {

    private static final long SETTING_ID = 1L;

    private final JoinSettingRepository joinSettingRepository;
    private final JoinGroupRepository joinGroupRepository;

    public JoinInfoService(JoinSettingRepository joinSettingRepository, JoinGroupRepository joinGroupRepository) {
        this.joinSettingRepository = joinSettingRepository;
        this.joinGroupRepository = joinGroupRepository;
    }

    public JoinInfoResponse publicInfo() {
        return new JoinInfoResponse(setting(), joinGroupRepository.findByVisibleTrueOrderBySortOrderAscIdAsc());
    }

    public JoinInfoResponse adminInfo() {
        return new JoinInfoResponse(setting(), joinGroupRepository.findAllByOrderBySortOrderAscIdAsc());
    }

    @Transactional
    public JoinSetting updateSetting(JoinSettingRequest request) {
        JoinSetting setting = setting();
        setting.setTitle(request.title());
        setting.setSubtitle(request.subtitle());
        setting.setManagerName(request.managerName());
        setting.setManagerWechatId(request.managerWechatId());
        setting.setManagerNote(request.managerNote());
        return joinSettingRepository.save(setting);
    }

    @Transactional
    public JoinGroup createGroup(JoinGroupRequest request) {
        JoinGroup group = new JoinGroup();
        return joinGroupRepository.save(apply(group, request));
    }

    @Transactional
    public JoinGroup updateGroup(Long id, JoinGroupRequest request) {
        JoinGroup group = joinGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("join group not found"));
        return joinGroupRepository.save(apply(group, request));
    }

    public void deleteGroup(Long id) {
        JoinGroup group = joinGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("join group not found"));
        joinGroupRepository.delete(group);
    }

    private JoinSetting setting() {
        return joinSettingRepository.findById(SETTING_ID).orElseGet(() -> {
            JoinSetting setting = new JoinSetting();
            setting.setId(SETTING_ID);
            return joinSettingRepository.save(setting);
        });
    }

    private JoinGroup apply(JoinGroup group, JoinGroupRequest request) {
        group.setName(request.name());
        group.setDescription(request.description());
        group.setQrUrl(request.qrUrl());
        group.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        group.setVisible(request.visible() == null || request.visible());
        return group;
    }
}
