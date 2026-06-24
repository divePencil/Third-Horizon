# 三人成行微信小程序

这个目录是原生微信小程序工程，复用当前 Spring Boot 后端接口。

## 已包含功能

- 微信登录：`POST /api/auth/wechat/login`
- 活动列表：复用 `GET /api/public/activities`
- 活动详情：复用 `GET /api/public/activities/{id}`
- 活动报名：`POST /api/user/activities/{id}/signup`
- 报名成功后显示活动临时群二维码
- 发布活动：`POST /api/user/activities`
- 我的活动：`GET /api/user/activities`
- 活动费用录入：`POST /api/user/activities/{id}/expenses`
- 活动算账：`POST /api/user/activities/{id}/settlement`
- 文件上传：`POST /api/user/uploads`

## 后端环境变量

生产环境需要配置：

```bash
WECHAT_APP_ID=你的小程序appid
WECHAT_SECRET=你的小程序secret
USER_TOKEN_SECRET=长随机字符串
USER_TOKEN_TTL_HOURS=720
```

如果没有配置 `WECHAT_APP_ID` 和 `WECHAT_SECRET`，后端会使用 `dev-${code}` 作为 openid，方便本地开发联调。

## 小程序开发配置

1. 使用微信开发者工具导入 `miniapp` 目录。
2. 修改 `miniapp/project.config.json` 里的 `appid`。
3. 修改 `miniapp/app.js`：

```js
apiBase: 'https://你的域名'
```

本地联调可临时改成：

```js
apiBase: 'http://127.0.0.1:18080'
```

微信开发者工具里需要勾选“不校验合法域名”，或在微信公众平台配置 request/uploadFile 合法域名。

## 接口复用原则

小程序没有重复建设网站已有接口：

- 展示类接口继续走 `/api/public/**`
- 管理后台继续走 `/api/admin/**`
- 微信登录用户能力新增在 `/api/user/**`

这样网站以后如果要做用户登录、用户报名、活动算账，也可以复用同一套接口。

## 后续建议

第一版跑通后，建议继续补：

- 活动成员显示昵称，而不是只显示用户 ID。
- 活动相册上传页面。
- 费用凭证图片上传。
- 成员确认已付款。
- 活动发起人审核报名。
- 小程序订阅消息提醒。

