package com.freediving.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "third_horizon_activity_publish_settings")
public class ActivityPublishSetting extends BaseEntity {

    @Id
    private Long id = 1L;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String disclaimerContent = "户外及水上活动存在天气、水况、地形、装备、交通和个人身体状态等不确定风险。报名者应如实评估自身能力，确认健康状态，自行承担个人行为产生的风险，并服从组织者的安全安排。";
}
