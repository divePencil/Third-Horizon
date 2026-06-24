# 三人成行生产部署记录

本文记录当前工程部署到阿里云服务器的完整过程。当前约定如下：

- 后端服务目录：`/opt/third_horizon/backend`
- 前端源码目录：`/opt/third_horizon/frontend`
- 前端静态目录：`/var/www/third_horizon`
- 数据库名：`third_horizon`
- 数据库用户：`third_horizon`
- 后端监听端口：`18080`
- Nginx 对外端口：`80`
- 后端服务名：`third-horizon-api`

> 注意：服务器上 `127.0.0.1:8080` 已被 SearXNG 占用，所以本项目后端不要使用 8080，统一使用 `18080`。

## 1. 服务器基础环境

已确认服务器具备：

```bash
java -version
mysql --version
nginx -v
```

当前环境：

- Java 17
- MySQL 8
- Nginx 1.24

如果 Nginx 未安装，可按服务器系统包管理器安装，例如：

```bash
sudo yum install -y nginx
sudo systemctl enable nginx
sudo systemctl start nginx
```

## 2. 创建目录

```bash
sudo mkdir -p /opt/third_horizon/backend
sudo mkdir -p /opt/third_horizon/frontend
sudo mkdir -p /var/www/third_horizon
sudo chown -R admin:admin /opt/third_horizon
```

如果当前登录用户不是 `admin`，把上面的 `admin:admin` 改成实际用户。

## 3. 初始化 MySQL

使用 root 登录 MySQL：

```bash
mysql -uroot -p
```

创建数据库和用户：

```sql
CREATE DATABASE third_horizon DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'third_horizon'@'localhost' IDENTIFIED BY '请替换成强密码';
GRANT ALL PRIVILEGES ON third_horizon.* TO 'third_horizon'@'localhost';
FLUSH PRIVILEGES;
```

如果用户已存在，只需要确认授权：

```sql
GRANT ALL PRIVILEGES ON third_horizon.* TO 'third_horizon'@'localhost';
FLUSH PRIVILEGES;
```

## 4. 本地打包后端

在本机工程目录执行：

```powershell
cd E:\freediving\backend
D:\apache-maven-3.9.9\bin\mvn.cmd clean package -DskipTests
```

工程已经在 `backend/pom.xml` 中配置：

```xml
<finalName>third-horizon-api-${project.version}</finalName>
```

打包完成后，后端 jar 位于：

```text
E:\freediving\backend\target\third-horizon-api-0.1.0.jar
```

上传到服务器：

```text
/opt/third_horizon/backend/third-horizon-api-0.1.0.jar
```

服务器上建议再复制一份固定运行文件名，后续升级 jar 时 systemd 不用改：

```bash
cd /opt/third_horizon/backend
cp third-horizon-api-0.1.0.jar third-horizon-api.jar
```

## 5. 配置后端环境变量

在服务器创建：

```bash
vi /opt/third_horizon/backend/.env
```

参考内容：

```bash
SERVER_PORT=18080

DB_URL=jdbc:mysql://localhost:3306/third_horizon?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
DB_USERNAME=third_horizon
DB_PASSWORD=请替换成数据库密码

ADMIN_USERNAME=admin
ADMIN_PASSWORD=请替换成后台管理密码
ADMIN_TOKEN_SECRET=请替换成长随机字符串
ADMIN_TOKEN_TTL_HOURS=12

COS_SECRET_ID=请替换
COS_SECRET_KEY=请替换
COS_REGION=ap-guangzhou
COS_BUCKET_NAME=请替换
COS_PUBLIC_BASE_URL=请替换成COS或CDN访问域名
COS_PUBLIC_READ=true

JPA_DDL_AUTO=update
```

建议限制权限：

```bash
chmod 600 /opt/third_horizon/backend/.env
```

## 6. 创建 systemd 后端服务

创建服务文件：

```bash
sudo vi /etc/systemd/system/third-horizon-api.service
```

内容：

```ini
[Unit]
Description=Third Horizon Spring Boot API
After=network.target mysql.service

[Service]
Type=simple
WorkingDirectory=/opt/third_horizon/backend
EnvironmentFile=/opt/third_horizon/backend/.env
ExecStart=/usr/bin/java -jar /opt/third_horizon/backend/third-horizon-api.jar
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

启动服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable third-horizon-api
sudo systemctl restart third-horizon-api
sudo systemctl status third-horizon-api
```

查看日志：

```bash
journalctl -u third-horizon-api -f
```

确认端口：

```bash
sudo ss -lntp | grep ':18080'
```

测试后端登录接口：

```bash
curl -X POST http://127.0.0.1:18080/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"请替换成ADMIN_PASSWORD"}'
```

如果这里返回 token，说明后端服务正常。

## 7. 构建并部署前端

本地构建：

```powershell
cd E:\freediving\frontend
npm install
npm run build
```

构建结果位于：

```text
E:\freediving\frontend\dist
```

把 `dist` 目录里的文件上传到服务器：

```text
/var/www/third_horizon
```

也可以先上传前端源码到：

```text
/opt/third_horizon/frontend
```

然后在服务器构建：

```bash
cd /opt/third_horizon/frontend
npm install
npm run build
sudo rm -rf /var/www/third_horizon/*
sudo cp -r dist/* /var/www/third_horizon/
```

## 8. 配置 Nginx

创建配置文件：

```bash
sudo vi /etc/nginx/conf.d/third_horizon.conf
```

内容：

```nginx
server {
    listen 80;
    server_name _;

    root /var/www/third_horizon;
    index index.html;

    client_max_body_size 35m;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:18080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

检查并重载：

```bash
sudo nginx -t
sudo systemctl reload nginx
```

如果出现：

```text
unknown directive "erver"
```

说明配置第一行写成了 `erver {`，应改成：

```nginx
server {
```

## 9. 验证访问

验证 Nginx 是否命中前端：

```bash
curl -I http://127.0.0.1/
```

验证 Nginx 是否正确代理后端：

```bash
curl -X POST http://127.0.0.1/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"请替换成ADMIN_PASSWORD"}'
```

如果通过 `http://127.0.0.1:18080/api/...` 能登录，但通过 `http://127.0.0.1/api/...` 不行，优先检查 Nginx 的 `/api/` 代理配置。

## 10. 常见问题

### 10.1 登录接口返回 SearXNG 页面

现象：

```bash
curl http://127.0.0.1:8080/api/auth/admin/login
```

返回 SearXNG HTML 页面。

原因：服务器的 8080 已被 SearXNG 占用，请使用本项目后端端口 `18080`。

处理：

```bash
grep SERVER_PORT /opt/third_horizon/backend/.env
sudo systemctl restart third-horizon-api
sudo ss -lntp | grep ':18080'
```

并确认 Nginx 配置：

```nginx
proxy_pass http://127.0.0.1:18080/api/;
```

### 10.2 `journalctl -u third-horizon-api -f` 没有新日志

通常说明请求没有打到后端，或服务没有启动成功。

按顺序检查：

```bash
sudo systemctl status third-horizon-api
sudo ss -lntp | grep ':18080'
curl -X POST http://127.0.0.1:18080/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"请替换成ADMIN_PASSWORD"}'
```

### 10.3 浏览器显示 Apache/httpd 默认页

现象：页面显示 `Welcome to HTTP Server Test Page!`。

说明当前 80 端口可能不是预期的站点配置，或者默认站点优先级更高。

检查监听：

```bash
sudo ss -lntp | grep ':80'
```

检查 Nginx 配置：

```bash
sudo nginx -t
sudo nginx -T | grep -n "server_name\|root /var/www"
```

确保本项目站点配置指向：

```nginx
root /var/www/third_horizon;
```

### 10.4 管理员密码错误

管理员密码来自：

```bash
grep ADMIN_PASSWORD /opt/third_horizon/backend/.env
```

修改 `.env` 后必须重启服务：

```bash
sudo systemctl restart third-horizon-api
```

然后再登录。

### 10.5 图片上传或大文件请求失败

确认 Nginx 里配置了：

```nginx
client_max_body_size 35m;
```

如果需要支持更大的图片或视频，可以继续调大。

## 11. 升级发布流程

后续每次发布可以按这个流程：

1. 本地后端打包：

```powershell
cd E:\freediving\backend
D:\apache-maven-3.9.9\bin\mvn.cmd clean package -DskipTests
```

2. 上传 `target/third-horizon-api-0.1.0.jar` 到服务器 `/opt/third_horizon/backend/`。

3. 替换运行 jar 并重启：

```bash
cd /opt/third_horizon/backend
cp third-horizon-api-0.1.0.jar third-horizon-api.jar
sudo systemctl restart third-horizon-api
sudo systemctl status third-horizon-api
```

4. 前端构建并上传 `dist` 内容到 `/var/www/third_horizon`。

5. 重载 Nginx：

```bash
sudo nginx -t
sudo systemctl reload nginx
```

6. 验证首页和后台登录。

