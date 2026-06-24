# 三人成行 | The Third Horizon

一个面向自由潜渔猎微信群的活动官网工程，包含：

- Spring Boot 后端 API
- Vue 3 前端页面
- MySQL 数据库
- 腾讯云 COS 图片/视频上传
- Nginx + systemd 部署样例

## 目录结构

```text
backend/   Spring Boot API
frontend/  Vue 3 + Vite 前端
deploy/    阿里云服务器部署参考
```

## 后端本地启动

本机推荐使用你已安装的 Maven 3.9.9：

```powershell
D:\apache-maven-3.9.9\bin\mvn.cmd -v
```

先创建 MySQL 数据库：

```sql
CREATE DATABASE freediving DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

设置环境变量后启动：

```bash
cd backend
D:\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run
```

Windows 下也可以直接运行：

```powershell
cd backend
.\run-dev.ps1
```

常用环境变量：

```bash
SERVER_PORT=8080
DB_URL=jdbc:mysql://localhost:3306/freediving?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
DB_USERNAME=root
DB_PASSWORD=your-password
ADMIN_USERNAME=admin
ADMIN_PASSWORD=replace-with-a-strong-password
ADMIN_TOKEN_SECRET=replace-with-a-long-random-secret
ADMIN_TOKEN_TTL_HOURS=12
COS_SECRET_ID=your-secret-id
COS_SECRET_KEY=your-secret-key
COS_REGION=ap-guangzhou
COS_BUCKET_NAME=your-bucket-1250000000
COS_PUBLIC_BASE_URL=https://your-cdn-or-cos-domain
```

接口文档：

```text
http://localhost:8080/swagger-ui.html
```

## 前端本地启动

当前工程前端依赖已按 Node 16 兼容版本选择：

- Node.js 16 可用：Vite 4
- 如果升级到 Node.js 18/20，后续可以再升级 Vite 5/6

```bash
cd frontend
npm install
npm run dev
```

如果你的当前终端设置了离线 npm 或无效代理，可以直接运行：

```powershell
cd frontend
.\run-dev.ps1
```

如果之前安装过不兼容版本依赖，先删除 `frontend/node_modules` 后再运行上面的脚本。

默认访问：

```text
http://localhost:5173
```

开发环境中，Vite 会把 `/api` 代理到 `http://localhost:8080`。

## 第一版功能

- 首页组织展示
- 活动计划列表
- 活动报名表
- 活动相册列表
- 新人指南文章
- 管理员账号密码登录与发布接口保护
- 腾讯云 COS 上传接口

## 管理接口鉴权

先调用登录接口获取管理员令牌：

```text
POST /api/auth/admin/login
Content-Type: application/json

{"username":"admin","password":"你的管理员密码"}
```

之后访问所有 `/api/admin/**` 接口时带上请求头：

```text
Authorization: Bearer 登录接口返回的 token
```

管理员账号密码来自环境变量 `ADMIN_USERNAME`、`ADMIN_PASSWORD`，令牌签名密钥来自 `ADMIN_TOKEN_SECRET`，默认有效期由 `ADMIN_TOKEN_TTL_HOURS` 控制。

## 腾讯云 COS 配置提醒

建议：

- Bucket 权限不要随意设为完全公开写入。
- 后端只保存 COS URL 和 object key。
- 图片公开访问可以使用 COS 默认域名或 CDN 自定义域名。
- 视频较大时建议走 CDN，避免服务器流量压力。
- 不要把 `COS_SECRET_ID` 和 `COS_SECRET_KEY` 写进代码仓库。

## 生产部署概览

1. 在阿里云安装 JDK 17、Maven、Node.js、Nginx、MySQL。
2. 构建后端 jar：`mvn clean package -DskipTests`
3. 构建前端：`npm run build`
4. 将 `frontend/dist` 放到 Nginx 静态目录。
5. 用 systemd 管理 Spring Boot 服务。
6. Nginx 将 `/api/` 反向代理到 `127.0.0.1:8080`。
7. 配置域名和 HTTPS。

部署样例见 `deploy/` 目录。
