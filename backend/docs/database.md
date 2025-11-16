# 数据库设计说明

本文档对 `bluemsun_blog` 库内主要表结构做简要说明，方便后续开发与维护。

## 总体设计

- **数据库类型**：MySQL 8.0+
- **编码**：UTF8MB4
- **命名约定**：业务表以 `t_` 开头，关联中间表以 `rel_` 开头；字段统一使用下划线风格。
- **通用字段**：所有业务表默认包含 `id`、`create_time`、`update_time`、`version`、`deleted` 等通用字段，对应 Java `BaseEntity`。
- **逻辑删除**：采用 `deleted` `TINYINT(1)`，0 表示有效，1 表示删除。
- **乐观锁**：`version` 字段，配合 MyBatis-Plus 自动处理并发更新。

## 核心表概览

| 表名 | 说明 | 关键字段 |
| --- | --- | --- |
| `t_user` | 用户基础信息 | `email`、`role`、`status`、`last_login_time` |
| `t_role` / `t_user_role` | 角色定义与关联 | `code`、`user_id`、`role_id` |
| `t_category` | 文章分类（支持递归） | `parent_id`、`sort` |
| `t_tag` / `rel_article_tag` | 标签体系与文章关联 | `slug`、`article_id`、`tag_id` |
| `t_article` | 文章主信息 | `title`、`type`、`status`、`price` |
| `t_article_content` | 文章正文、附件元数据 | `content_md`、`content_html`、`attachments(JSON)` |
| `t_article_stat` | 文章统计指标 | `view_count`、`like_count`、`pay_count` |
| `t_comment` | 评论树 | `parent_id`、`root_id`、`status` |
| `t_order` | 付费订单 | `order_no`、`status`、`amount`、`pay_channel` |
| `t_attachment` | 文章附件 | `access_scope`、`storage_path` |
| `t_ai_session` | AI 助手调用记录 | `scene`、`token_usage`、`status` |
| `t_login_log` | 用户登录日志 | `client_ip`、`user_agent` |
| `t_site_setting` | 站点配置 | `key`、`value(JSON)` |

## 设计要点

1. **付费模型**：`t_article` 中 `type` 字段区分公开与付费；`t_order` 记录用户购买行为，`uk_order_user_article` 用于确保一篇付费文章只需购买一次。
2. **评论盖楼**：`parent_id` 与 `root_id` 支持多级回复；通过索引优化查询同楼层评论。
3. **文章内容拆分**：正文与统计、元数据分离，减少主表压力，提升列表查询性能。
4. **附件权限**：`access_scope` 标识附件可见范围；后端根据订单信息判定下载权限。
5. **AI 资源计费**：`t_ai_session` 后续可扩展计费策略（如每日配额、Token 消耗计数等）。
6. **配置中心**：`t_site_setting` 使用 `key-value` 形式存储站点与支付等配置，方便后台管理端维护。

## 后续扩展建议

- 若需更细粒度的权限，可增加 `t_permission`、`t_role_permission`；
- 若支付渠道接入第三方平台，可新增 `t_payment_notify` 保存异步回调记录；
- 如需支持多作者或团队协作，可在 `t_article` 中增加 `co_author_ids`（JSON）或拆分关联表；
- 建议结合 Redis 实现热门文章缓存、验证码、防刷等；
- 可基于 `t_login_log` 构建风控规则（异常登录提醒）。

> 详细建表 SQL 请参考 `../sql/schema.sql`。

