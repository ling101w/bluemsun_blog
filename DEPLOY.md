# 部署指南

本文档介绍如何将本项目部署到一台 Linux 服务器（以 `Alibaba Cloud Linux 8` 为例，其他发行版仅需替换包管理器命令）。

## 1. 运行环境

| 组件 | 版本建议 | 安装方式 |
| ---- | -------- | -------- |
| JDK  | OpenJDK 17 | `dnf install -y java-17-openjdk java-17-openjdk-devel` |
| Maven | 3.8+ | `dnf install -y maven` |
| Node.js / npm | 18.x | `curl -fsSL https://rpm.nodesource.com/setup_18.x \| bash - && dnf install -y nodejs` |
| MySQL | 8.x | 按官方文档启用 `mysql80-community` 仓库后 `dnf install -y mysql-community-server` |
| Redis | 6.x+ | `dnf install -y redis` |
| Nginx | 最新稳定版 | `dnf install -y nginx` 或者使用宝塔面板 |

> MySQL 安装后记得执行 `ALTER USER 'root'@'localhost' IDENTIFIED BY '强密码';` 并创建数据库 `CREATE DATABASE bluemsun_blog DEFAULT CHARACTER SET utf8mb4;`

## 2. 配置说明

1. **后端配置**  
   编辑 `backend/src/main/resources/application.yml`（或另建 `application-prod.yml`），设置：
   - `spring.datasource.*`
   - `spring.data.redis.*`
   - OSS、邮件、AI 等可暂时留空或禁用。

2. **前端配置**  
   已改为默认读取 `VITE_API_BASE_URL=/api`，若使用 Nginx 反向代理 `/api` → 后端，即可无需再改。  
   如需自定义，可以在 `.env.production` 中设置：
   ```
   VITE_API_BASE_URL=https://your-domain.com/api
   ```

## 3. 构建产物

### 3.1 后端 Jar
```bash
cd backend
mvn -DskipTests package
# 产物：backend/target/bluemsun-blog-0.0.1-SNAPSHOT.jar
```
后台运行示例：
```bash
nohup java -jar target/bluemsun-blog-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > /var/log/bluemsun-blog.log 2>&1 &
```

### 3.2 前端静态文件
```bash
cd frontend
npm install
npm run build
# 产物：frontend/dist/*
```
将 `dist` 内容复制到服务器 Nginx/宝塔站点根目录。

## 4. Nginx（或宝塔反向代理）示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /www/wwwroot/bluemsun-frontend;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

> 宝塔面板：在“站点”中上传前端 `dist` 文件；“反向代理”添加 `/api` → `http://127.0.0.1:8080/api/`，并开启 `KeepAlive`、`代理缓存` 视情况配置。

## 5. 验证

1. 浏览器访问 `http(s)://your-domain.com`，应加载前端页面。  
2. 用 `curl http://127.0.0.1:8080/api/articles` 检查后端。  
3. 查看后台日志 `tail -f /var/log/bluemsun-blog.log`。  
4. 若启用 HTTPS，可用 `certbot --nginx -d your-domain.com` 快速申请 Let’s Encrypt 证书。

## 6. 常见问题

| 问题 | 解决 |
| ---- | ---- |
| 502 Bad Gateway | 检查后端是否启动、Nginx 反向代理目标地址是否正确。 |
| CORS 报错 | 确保前端请求 `/api`，由 Nginx 代理到后端，同域访问即可；或在后端 `WebMvcConfig` 里添加 CORS 配置。 |
| HeroBanner 404 | 若暂未实现 `/api/common/bing-daily`，可在 `HeroBanner.vue` 中捕获异常后回退到默认图片。 |

至此，服务器即可运行 `dist` 前端 + Spring Boot Jar 后端。如需进一步的自动化（systemd、CI/CD 等），可在此基础上扩展。

