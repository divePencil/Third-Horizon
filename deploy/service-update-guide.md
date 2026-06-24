# 三人成行服务更新操作文档

本文用于记录项目已经部署成功后，后续每次更新后端或前端的操作流程。

当前生产环境约定：

- 后端目录：`/opt/third_horizon/backend`
- 前端源码目录：`/opt/third_horizon/frontend`
- 前端静态目录：`/var/www/third_horizon`
- 后端服务名：`third-horizon-api`
- 后端端口：`18080`
- Nginx 端口：`80`
- 本地工程目录：`E:\freediving`
- 本地 Maven：`D:\apache-maven-3.9.9\bin\mvn.cmd`

## 1. 更新前检查

服务器上先确认当前服务正常：

```bash
sudo systemctl status third-horizon-api
sudo ss -lntp | grep ':18080'
curl -I http://127.0.0.1/
```

查看当前后端包：

```bash
ls -lh /opt/third_horizon/backend
```

建议每次更新前先备份当前 jar：

```bash
cd /opt/third_horizon/backend
cp third-horizon-api.jar third-horizon-api.jar.bak.$(date +%Y%m%d%H%M%S)
```

如果要更新前端，也建议备份当前静态文件：

```bash
sudo tar -czf /opt/third_horizon/frontend-dist.bak.$(date +%Y%m%d%H%M%S).tar.gz -C /var/www/third_horizon .
```

## 2. 更新后端

### 2.1 本地打包

在本机执行：

```powershell
cd E:\freediving\backend
D:\apache-maven-3.9.9\bin\mvn.cmd clean package -DskipTests
```

打包结果：

```text
E:\freediving\backend\target\third-horizon-api-0.1.0.jar
```

### 2.2 上传 jar

把本地 jar 上传到服务器：

```text
/opt/third_horizon/backend/third-horizon-api-0.1.0.jar
```

可以用 Xftp、FinalShell、scp 或阿里云控制台上传。

如果使用 scp，示例：

```powershell
scp E:\freediving\backend\target\third-horizon-api-0.1.0.jar admin@你的服务器IP:/opt/third_horizon/backend/
```

### 2.3 替换运行包

服务器执行：

```bash
cd /opt/third_horizon/backend
cp third-horizon-api-0.1.0.jar third-horizon-api.jar
```

### 2.4 重启后端

```bash
sudo systemctl restart third-horizon-api
sudo systemctl status third-horizon-api
```

如果状态不是 `active (running)`，查看日志：

```bash
journalctl -u third-horizon-api -n 100 --no-pager
```

实时看日志：

```bash
journalctl -u third-horizon-api -f
```

### 2.5 验证后端

直接访问后端端口：

```bash
curl -X POST http://127.0.0.1:18080/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"你的管理员密码"}'
```

通过 Nginx 访问：

```bash
curl -X POST http://127.0.0.1/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"你的管理员密码"}'
```

两个接口都能正常返回 token，说明后端更新成功。

## 3. 更新前端

### 3.1 本地构建

在本机执行：

```powershell
cd E:\freediving\frontend
npm install
npm run build
```

构建结果：

```text
E:\freediving\frontend\dist
```

### 3.2 上传静态文件

把 `dist` 目录里的所有文件上传到服务器：

```text
/var/www/third_horizon
```

注意：上传的是 `dist` 里面的内容，不是把整个 `dist` 文件夹放进去。

也就是服务器上应该能看到：

```text
/var/www/third_horizon/index.html
/var/www/third_horizon/assets/...
```

### 3.3 服务器替换文件

如果先上传到了服务器临时目录，例如 `/opt/third_horizon/frontend/dist`，可以这样替换：

```bash
sudo rm -rf /var/www/third_horizon/*
sudo cp -r /opt/third_horizon/frontend/dist/* /var/www/third_horizon/
sudo chown -R nginx:nginx /var/www/third_horizon 2>/dev/null || true
```

如果系统里 Nginx 用户不是 `nginx`，上面的 `chown` 失败可以忽略，只要 Nginx 能读取文件即可。

### 3.4 重载 Nginx

```bash
sudo nginx -t
sudo systemctl reload nginx
```

### 3.5 验证前端

服务器本机验证：

```bash
curl -I http://127.0.0.1/
```

浏览器访问：

```text
http://你的服务器IP/
```

如果页面没变化，优先尝试浏览器强制刷新：

- Windows：`Ctrl + F5`
- Mac：`Command + Shift + R`

## 4. 同时更新前后端的推荐顺序

如果一次更新同时包含后端和前端，推荐顺序：

1. 备份当前后端 jar 和前端静态文件。
2. 上传新的后端 jar。
3. 重启后端并验证接口。
4. 上传新的前端 dist 文件。
5. 重载 Nginx。
6. 浏览器验证首页、活动页、后台登录、图片上传等关键功能。

不要在后端还没验证成功前就覆盖前端，方便定位问题。

## 5. 回滚后端

先查看备份：

```bash
ls -lh /opt/third_horizon/backend/third-horizon-api.jar.bak.*
```

选择一个备份恢复：

```bash
cd /opt/third_horizon/backend
cp third-horizon-api.jar.bak.你的备份时间 third-horizon-api.jar
sudo systemctl restart third-horizon-api
sudo systemctl status third-horizon-api
```

验证：

```bash
curl -X POST http://127.0.0.1:18080/api/auth/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"你的管理员密码"}'
```

## 6. 回滚前端

查看备份：

```bash
ls -lh /opt/third_horizon/frontend-dist.bak.*.tar.gz
```

选择一个备份恢复：

```bash
sudo rm -rf /var/www/third_horizon/*
sudo tar -xzf /opt/third_horizon/frontend-dist.bak.你的备份时间.tar.gz -C /var/www/third_horizon
sudo nginx -t
sudo systemctl reload nginx
```

浏览器强制刷新后验证。

## 7. 常用排错命令

查看后端状态：

```bash
sudo systemctl status third-horizon-api
```

查看后端日志：

```bash
journalctl -u third-horizon-api -n 100 --no-pager
journalctl -u third-horizon-api -f
```

查看端口占用：

```bash
sudo ss -lntp | grep ':18080'
sudo ss -lntp | grep ':80'
```

检查 Nginx 配置：

```bash
sudo nginx -t
sudo nginx -T | grep -n "third_horizon\|proxy_pass\|root"
```

测试后端直连：

```bash
curl http://127.0.0.1:18080/api/activities
```

测试 Nginx 代理：

```bash
curl http://127.0.0.1/api/activities
```

## 8. 更新检查清单

每次发布后建议检查：

- 首页是否正常打开。
- 活动列表是否正常展示。
- 活动详情是否正常展示。
- 后台登录是否正常。
- 新建/编辑/删除活动是否正常。
- 图片上传是否正常。
- 相册列表和相册详情是否正常。
- 加入组织入口是否正常。

