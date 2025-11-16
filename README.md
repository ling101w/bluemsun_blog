# 蓝旭博客 Monorepo

一个包含 Spring Boot 3 后端 + Vue 3 前端的全栈博客/内容社区项目，支持付费文章、OSS 附件、AI 助手、后台管理等功能。

## 功能亮点

- ✍️ **创作与发布**：文章草稿/发布、Markdown 正文、封面与摘要，支持免费/付费类型及附件上传。
- 💬 **互动体系**：树形评论、表情解析、点赞限流、邮件通知以及 Redis 防刷。
- 💰 **交易闭环**：订单创建、余额支付/外部支付链接、热门作者与付费附件授权判定。
- 🧾 **后台管理**：用户/文章/订单可视化管理，余额充值、发布/下线/公告置顶、统计概览。
- ☁️ **存储与 AI**：阿里云 OSS 统一文件服务，SiliconFlow + DeepSeek 提供的写作助手。
- 📦 **部署友好**：仓库内置 `DEPLOY.md`，提供一键构建与 Nginx 反代示例。

## 技术栈

| 层级 | 技术 |
| ---- | ---- |
| 后端 | Spring Boot 3.2 · MyBatis-Plus · Sa-Token · Redis · Aliyun OSS SDK |
| 前端 | Vite 6 · Vue 3 + TypeScript · Pinia · Vue Router · Element Plus |
| 其他 | MySQL 8 · Redis 6 · Node.js 18+ · Maven 3.8+ |

## 目录结构

```
├─ backend/   # Spring Boot 应用
├─ frontend/  # Vite + Vue3 SPA
├─ storage/   # 本地上传占位目录（已被 .gitignore 忽略）
└─ DEPLOY.md  # 服务器部署说明
```

## 快速开始

### 1. 准备环境

- 安装 JDK 17、Maven、MySQL、Redis、Node.js 18。
- 申请阿里云 OSS、邮件 SMTP、SiliconFlow API 相关凭据。

### 2. 后端

```bash
cd backend
# 配置环境变量（示例）
set SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/bluemsun_blog?...
set SPRING_DATASOURCE_USERNAME=root
set SPRING_DATASOURCE_PASSWORD=your_password
set SPRING_REDIS_HOST=localhost
set OSS_ACCESS_KEY_ID=xxx
set OSS_ACCESS_KEY_SECRET=xxx
set AI_SILICONFLOW_API_KEY=xxx

mvn spring-boot:run
```

> 所有敏感配置均可通过环境变量覆盖，参考 `backend/src/main/resources/application.yml` 内的占位符。

### 3. 前端

```bash
cd frontend
npm install
npm run dev

# 生产构建
npm run build
```

Vite dev server 默认监听 `5173`，开发阶段可通过 `VITE_API_BASE_URL=http://localhost:8080/api` 指向本地后端。

## 构建 & 部署

1. **后端**：`mvn -DskipTests package`，生成 `backend/target/bluemsun-blog-0.0.1-SNAPSHOT.jar`。
2. **前端**：`npm run build`，产物位于 `frontend/dist/`，可由 Nginx/宝塔托管。
3. **反向代理**：参考 `DEPLOY.md` 中的 Nginx 示例，将 `/api` 代理至 `127.0.0.1:8080/api/`。

## 环境变量速查

| 键 | 说明 |
| --- | --- |
| `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` | MySQL 连接信息 |
| `SPRING_REDIS_HOST/PORT/PASSWORD` | Redis 配置 |
| `SPRING_MAIL_HOST/PORT/USERNAME/PASSWORD/SPRING_MAIL_FROM` | SMTP 邮箱 |
| `OSS_ENDPOINT/OSS_BUCKET/OSS_ACCESS_KEY_ID/OSS_ACCESS_KEY_SECRET/OSS_PUBLIC_DOMAIN` | OSS 存储 |
| `AI_SILICONFLOW_API_KEY/AI_SILICONFLOW_BASE_URL/AI_SILICONFLOW_MODEL` | AI 助手配置 |

## 参考资源

- `DEPLOY.md`：生产部署全过程。
- `backend/docs/database.md` & `backend/sql/schema.sql`：数据库设计与建表脚本。

欢迎根据业务需要扩展更多模块，如多作者协作、第三方支付回调、权限细粒度控制等。Happy hacking! 🎉
