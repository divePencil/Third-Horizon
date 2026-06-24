# 阿里云部署步骤

以下路径是建议值，可以按你的服务器习惯调整。

## 1. 准备目录

```bash
sudo mkdir -p /opt/freediving/backend /var/www/freediving
```

## 2. 后端构建

```bash
cd backend
mvn clean package -DskipTests
```

Windows 本机可使用：

```powershell
D:\apache-maven-3.9.9\bin\mvn.cmd clean package -DskipTests
```

把生成的 jar 上传到：

```text
/opt/freediving/backend/freediving-api.jar
```

把 `backend/.env.example` 复制为：

```text
/opt/freediving/backend/.env
```

并填写数据库、管理员 Token、腾讯云 COS 配置。

## 3. systemd 服务

```bash
sudo cp deploy/freediving-api.service /etc/systemd/system/freediving-api.service
sudo systemctl daemon-reload
sudo systemctl enable freediving-api
sudo systemctl start freediving-api
sudo systemctl status freediving-api
```

## 4. 前端构建

```bash
cd frontend
npm install
npm run build
```

把 `frontend/dist/` 内的文件上传到：

```text
/var/www/freediving
```

## 5. Nginx

修改 `deploy/nginx.conf` 里的 `server_name`，然后复制到 Nginx 配置目录。

```bash
sudo nginx -t
sudo systemctl reload nginx
```

## 6. HTTPS

可以使用阿里云免费证书，或用 Certbot 申请 Let's Encrypt 证书。

生产环境建议把 `/api/admin/**` 只通过 HTTPS 访问，并把 `ADMIN_TOKEN` 设置成长随机字符串。
