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
@Table(name = "third_horizon_join_settings")
public class JoinSetting extends BaseEntity {

    @Id
    private Long id = 1L;

    @Column(nullable = false, length = 120)
    private String title = "加入三人成行";

    @Column(nullable = false, length = 120)
    private String subtitle = "一起看见无界山海";

    @Column(nullable = false, length = 120)
    private String managerName = "三人成行管理员";

    @Column(nullable = false, length = 120)
    private String managerWechatId = "请在后台维护管理员微信号";

    @Column(columnDefinition = "TEXT")
    private String managerNote = "添加时备注：三人成行 + 昵称 + 想参与的活动";
}
