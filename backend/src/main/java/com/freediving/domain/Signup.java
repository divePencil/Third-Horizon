package com.freediving.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "third_horizon_signups")
public class Signup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long activityId;

    @Column(nullable = false, length = 60)
    private String nickname;

    @Column(length = 80)
    private String wechatId;

    @Column(length = 40)
    private String phone;

    @Column(length = 80)
    private String emergencyContact;

    @Column(length = 40)
    private String emergencyPhone;

    @Column(length = 120)
    private String experienceLevel;

    private Boolean hasInsurance = false;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SignupStatus status = SignupStatus.PENDING;
}
