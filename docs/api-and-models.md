# 数据模型与接口清单

## 主要数据表

### third_horizon_activities

活动计划。

- `title`：活动标题
- `location`：地点概述，不建议公开精确点位
- `startTime` / `endTime`：活动时间
- `capacity`：人数上限
- `feeCents`：费用，单位分
- `aa`：是否 AA，默认 `true`
- `feeDescription`：费用说明
- `tempGroupQrUrl`：活动临时群二维码
- `status`：`DRAFT` / `OPEN` / `FULL` / `CLOSED` / `CANCELLED` / `FINISHED`
- `visibility`：`PUBLIC` / `MEMBERS`
- `coverUrl`：封面图
- `summary`：活动简介
- `requirements`：装备和经验要求
- `safetyNotes`：安全说明
- `activityContents`：活动项目选项，JSON 字符串数组
- `joinConditions`：加入条件，JSON 字符串数组
- `equipmentItems`：装备清单，JSON 字符串数组
- `meetingLocation` / `meetingTime` / `meetingMapUrl`：集合信息
- `itinerary`：行程安排
- `destinationName` / `destinationMapUrl` / `destinationFacilities`：目的地信息
- `disclaimerRequired`：是否要求确认免责申明

### third_horizon_signups

活动报名。

- `activityId`：活动 ID
- `nickname`：昵称
- `wechatId`：微信号
- `phone`：手机号
- `emergencyContact` / `emergencyPhone`：紧急联系人
- `experienceLevel`：自由潜或渔猎经验
- `hasInsurance`：是否有保险
- `status`：`PENDING` / `APPROVED` / `REJECTED` / `CANCELLED`

### third_horizon_albums

活动相册。

- `activityId`：关联活动，可为空
- `title`：相册标题
- `location`：地点概述
- `activityDate`：活动日期
- `coverUrl`：封面图
- `story`：活动故事
- `visibility`：公开范围

### third_horizon_media_assets

相册媒体文件。

- `albumId`：相册 ID
- `url`：COS 访问 URL
- `objectKey`：COS 对象 key
- `title` / `caption`：标题和说明
- `type`：`IMAGE` / `VIDEO`
- `sortOrder`：排序

### third_horizon_articles

组织介绍、新人指南、群规、安全规范等内容。

- `title`：标题
- `category`：所属模块，如新人指南、组织介绍、活动说明、装备知识、安全规范
- `coverUrl`：封面图
- `excerpt`：摘要
- `content`：正文
- `visibility`：公开范围
- `published`：是否发布

### third_horizon_activity_publish_settings

活动发布默认配置，固定使用一条记录。

- `disclaimerContent`：默认免责申明内容

### third_horizon_activity_options

活动发布可选项。

- `category`：`CONTENT` 活动项目 / `JOIN_CONDITION` 加入条件 / `EQUIPMENT` 装备清单
- `activityType`：装备所属活动项目，从已配置的活动项目中选择
- `label`：选项名称
- `sortOrder`：排序
- `visible`：是否在发布活动时显示

### third_horizon_join_settings

加入组织入口的全局信息，固定使用一条记录。

- `title`：加入弹窗标题
- `subtitle`：加入弹窗副标题
- `managerName`：管理员名称
- `managerWechatId`：管理员微信号
- `managerNote`：添加管理员时的备注提示

### third_horizon_join_groups

加入组织入口中的微信群列表。

- `name`：微信群名称
- `description`：微信群介绍
- `qrUrl`：群二维码 COS URL
- `sortOrder`：排序值，越小越靠前
- `visible`：是否在前台显示

## 公开接口

- `GET /api/public/activities`
- `GET /api/public/activities/{id}`
- `POST /api/public/signups`
- `GET /api/public/albums`
- `GET /api/public/albums/{id}`
- `GET /api/public/albums/{id}/media`
- `GET /api/public/articles`
- `GET /api/public/join-info`
- `GET /api/public/activity-publish-config`

## 管理接口

登录：

```text
POST /api/auth/admin/login
```

请求头：

```text
Authorization: Bearer your-login-token
```

- `GET /api/admin/activities`
- `POST /api/admin/activities`
- `PUT /api/admin/activities/{id}`
- `GET /api/admin/albums`
- `POST /api/admin/albums`
- `PUT /api/admin/albums/{id}`
- `POST /api/admin/media`
- `GET /api/admin/articles`
- `POST /api/admin/articles`
- `PUT /api/admin/articles/{id}`
- `GET /api/admin/activity-publish-config`
- `PUT /api/admin/activity-publish-config/setting`
- `POST /api/admin/activity-publish-config/options`
- `PUT /api/admin/activity-publish-config/options/{id}`
- `DELETE /api/admin/activity-publish-config/options/{id}`
- `GET /api/admin/join-info`
- `PUT /api/admin/join-info/setting`
- `POST /api/admin/join-info/groups`
- `PUT /api/admin/join-info/groups/{id}`
- `DELETE /api/admin/join-info/groups/{id}`
- `GET /api/admin/signups`
- `GET /api/admin/signups?activityId=1`
- `POST /api/admin/uploads`

## 后续建议

- 管理端增加操作日志、角色权限或微信登录。
- 报名增加审核接口和导出 Excel。
- 相册增加批量上传、拖拽排序、成员可见权限。
- 增加活动取消通知和报名候补名单。
- 给公开内容增加 SEO 元信息和分享海报。
