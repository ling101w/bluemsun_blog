-- -------------------------------------------------------------
-- 蓝旭博客项目 - 数据库初始化脚本
-- MySQL 8.0+
-- 在执行前请先创建数据库：
--   CREATE DATABASE IF NOT EXISTS bluemsun_blog DEFAULT CHARSET utf8mb4;
-- -------------------------------------------------------------

USE bluemsun_blog;

-- 为避免外键顺序问题，按依赖关系先删除子表再删除父表
DROP TABLE IF EXISTS t_attachment;
DROP TABLE IF EXISTS t_article_stat;
DROP TABLE IF EXISTS t_article_content;
DROP TABLE IF EXISTS rel_article_tag;
DROP TABLE IF EXISTS t_comment;
DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_ai_session;
DROP TABLE IF EXISTS t_login_log;
DROP TABLE IF EXISTS t_user_role;
DROP TABLE IF EXISTS t_article;
DROP TABLE IF EXISTS t_tag;
DROP TABLE IF EXISTS t_category;
DROP TABLE IF EXISTS t_role;
DROP TABLE IF EXISTS t_user;
DROP TABLE IF EXISTS t_site_setting;

-- -------------------------------------------------------------
-- 用户模块
-- -------------------------------------------------------------
CREATE TABLE t_user (
    id              BIGINT       PRIMARY KEY COMMENT '主键',
    email           VARCHAR(128) NOT NULL COMMENT '邮箱，唯一登录名',
    password        VARCHAR(128) NOT NULL COMMENT '密码（BCrypt）',
    nickname        VARCHAR(64)  NOT NULL COMMENT '昵称',
    avatar          VARCHAR(255) NULL COMMENT '头像地址',
    profile         VARCHAR(512) NULL COMMENT '个人简介',
    role            VARCHAR(32)  NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
    status          VARCHAR(16)  NOT NULL DEFAULT 'active' COMMENT '状态：active/disabled/blacklist',
    last_login_time DATETIME     NULL COMMENT '最近登录时间',
    balance         DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '账户余额',
    version         INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    deleted         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-否 1-是',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '用户表';

CREATE TABLE t_role (
    id          BIGINT PRIMARY KEY COMMENT '主键',
    code        VARCHAR(64)  NOT NULL COMMENT '角色编码，例如 ADMIN、USER',
    name        VARCHAR(64)  NOT NULL COMMENT '角色名称',
    description VARCHAR(255) NULL COMMENT '角色描述',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    deleted     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '角色定义表';

CREATE TABLE t_user_role (
    id          BIGINT PRIMARY KEY COMMENT '主键',
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    role_id     BIGINT NOT NULL COMMENT '角色ID',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    deleted     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_role_user (user_id),
    KEY idx_user_role_role (role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES t_role (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '用户 - 角色关联表';

CREATE TABLE t_login_log (
    id          BIGINT PRIMARY KEY COMMENT '主键',
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    client_ip   VARCHAR(64)  NULL COMMENT '客户端 IP',
    user_agent  VARCHAR(255) NULL COMMENT 'UA 信息',
    login_time  DATETIME     NOT NULL COMMENT '登录时间',
    success     TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否成功',
    message     VARCHAR(255) NULL COMMENT '备注信息',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    deleted     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除 0/1',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_login_log_user (user_id),
    CONSTRAINT fk_login_log_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '登录日志表';

-- -------------------------------------------------------------
-- 分类 / 标签
-- -------------------------------------------------------------
CREATE TABLE t_category (
    id          BIGINT PRIMARY KEY COMMENT '主键',
    name        VARCHAR(64)  NOT NULL COMMENT '分类名称',
    slug        VARCHAR(64)  NOT NULL COMMENT '分类别名，用于路由',
    parent_id   BIGINT       NULL COMMENT '父级ID，顶级为NULL',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    description VARCHAR(255) NULL COMMENT '描述',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    deleted     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_category_slug (slug),
    KEY idx_category_parent (parent_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '文章分类';

CREATE TABLE t_tag (
    id          BIGINT PRIMARY KEY COMMENT '主键',
    name        VARCHAR(64)  NOT NULL COMMENT '标签名称',
    slug        VARCHAR(64)  NOT NULL COMMENT '标签别名',
    description VARCHAR(255) NULL COMMENT '标签描述',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    deleted     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_tag_slug (slug)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '标签表';

-- -------------------------------------------------------------
-- 文章、附件及统计
-- -------------------------------------------------------------
CREATE TABLE t_article (
    id             BIGINT        PRIMARY KEY COMMENT '主键',
    author_id      BIGINT        NOT NULL COMMENT '作者ID',
    title          VARCHAR(255)  NOT NULL COMMENT '文章标题',
    summary        VARCHAR(512)  NULL COMMENT '摘要',
    cover_url      VARCHAR(255)  NULL COMMENT '封面图',
    category_id    BIGINT        NULL COMMENT '分类ID',
    type           VARCHAR(16)   NOT NULL DEFAULT 'FREE' COMMENT '类型：FREE/PAID',
    price          DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '售价，单位元',
    status         VARCHAR(16)   NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PUBLISHED/OFFLINE',
    publish_time   DATETIME      NULL COMMENT '发布时间',
    allow_comment  TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '是否允许评论',
    pinned         TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否公告置顶',
    pinned_time    DATETIME      NULL COMMENT '置顶时间',
    version        INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    deleted        TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    KEY idx_article_author (author_id),
    KEY idx_article_category (category_id),
    KEY idx_article_status (status),
    CONSTRAINT fk_article_author FOREIGN KEY (author_id) REFERENCES t_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_article_category FOREIGN KEY (category_id) REFERENCES t_category (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '文章主表';

CREATE TABLE t_article_content (
    id             BIGINT     PRIMARY KEY COMMENT '主键，与文章ID一致',
    content_md     LONGTEXT   NOT NULL COMMENT 'Markdown 内容',
    content_html   LONGTEXT   NULL COMMENT '渲染后的 HTML',
    attachments    JSON       NULL COMMENT '附件摘要（JSON 数组）',
    create_time    DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_article_content FOREIGN KEY (id) REFERENCES t_article (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '文章内容表';

CREATE TABLE rel_article_tag (
    id          BIGINT PRIMARY KEY COMMENT '主键',
    article_id  BIGINT NOT NULL COMMENT '文章ID',
    tag_id      BIGINT NOT NULL COMMENT '标签ID',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    deleted     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_article_tag (article_id, tag_id),
    KEY idx_article_tag_article (article_id),
    KEY idx_article_tag_tag (tag_id),
    CONSTRAINT fk_article_tag_article FOREIGN KEY (article_id) REFERENCES t_article (id) ON DELETE CASCADE,
    CONSTRAINT fk_article_tag_tag FOREIGN KEY (tag_id) REFERENCES t_tag (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '文章 - 标签关联';

CREATE TABLE t_article_stat (
    id            BIGINT PRIMARY KEY COMMENT '主键，与文章ID一致',
    view_count    BIGINT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    like_count    BIGINT NOT NULL DEFAULT 0 COMMENT '点赞次数',
    comment_count BIGINT NOT NULL DEFAULT 0 COMMENT '评论数',
    pay_count     BIGINT NOT NULL DEFAULT 0 COMMENT '付费次数',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_article_stat FOREIGN KEY (id) REFERENCES t_article (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '文章统计信息';

CREATE TABLE t_attachment (
    id            BIGINT       PRIMARY KEY COMMENT '主键',
    article_id    BIGINT       NOT NULL COMMENT '所属文章',
    file_name     VARCHAR(255) NOT NULL COMMENT '文件名',
    original_name VARCHAR(255) NULL COMMENT '原始文件名',
    content_type  VARCHAR(128) NULL COMMENT 'MIME 类型',
    file_size     BIGINT       NOT NULL COMMENT '文件大小（字节）',
    storage_path  VARCHAR(512) NOT NULL COMMENT '存储路径',
    download_url  VARCHAR(255) NULL COMMENT '对外访问路径',
    access_scope  VARCHAR(16)  NOT NULL DEFAULT 'PUBLIC' COMMENT '权限：PUBLIC/PAID_ONLY',
    version       INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    deleted       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_attachment_article (article_id),
    CONSTRAINT fk_attachment_article FOREIGN KEY (article_id) REFERENCES t_article (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '文章附件表';

-- -------------------------------------------------------------
-- 评论
-- -------------------------------------------------------------
CREATE TABLE t_comment (
    id           BIGINT      PRIMARY KEY COMMENT '主键',
    article_id   BIGINT      NOT NULL COMMENT '文章ID',
    user_id      BIGINT      NOT NULL COMMENT '评论用户',
    parent_id    BIGINT      NULL COMMENT '父评论ID',
    root_id      BIGINT      NULL COMMENT '楼层根评论ID',
    content      TEXT        NOT NULL COMMENT '评论内容',
    status       VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/APPROVED/REJECTED',
    like_count   INT         NOT NULL DEFAULT 0 COMMENT '点赞数',
    version      INT         NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    deleted      TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_comment_article (article_id),
    KEY idx_comment_user (user_id),
    KEY idx_comment_parent (parent_id),
    CONSTRAINT fk_comment_article FOREIGN KEY (article_id) REFERENCES t_article (id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '评论表';

-- -------------------------------------------------------------
-- 订单（付费内容）
-- -------------------------------------------------------------
CREATE TABLE t_order (
    id             BIGINT        PRIMARY KEY COMMENT '主键',
    order_no       VARCHAR(64)   NOT NULL COMMENT '订单号',
    user_id        BIGINT        NOT NULL COMMENT '购买用户',
    article_id     BIGINT        NOT NULL COMMENT '购买的付费文章',
    amount         DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    status         VARCHAR(16)   NOT NULL DEFAULT 'CREATED' COMMENT '状态：CREATED/PAYING/PAID/CLOSED',
    pay_channel    VARCHAR(32)   NULL COMMENT '支付渠道：MOCK_PAY/ALIPAY/WECHAT',
    pay_time       DATETIME      NULL COMMENT '支付时间',
    expire_time    DATETIME      NULL COMMENT '订单超时时间',
    version        INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    deleted        TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_order_user (user_id),
    KEY idx_order_article (article_id),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_article FOREIGN KEY (article_id) REFERENCES t_article (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '订单表';

-- -------------------------------------------------------------
-- AI 助手使用记录
-- -------------------------------------------------------------
CREATE TABLE t_ai_session (
    id             BIGINT PRIMARY KEY COMMENT '主键',
    user_id        BIGINT       NOT NULL COMMENT '用户ID',
    scene          VARCHAR(32)  NOT NULL COMMENT '场景：POLISH/EXTEND/SUMMARY/TRANSLATE/CONTINUE',
    prompt         TEXT         NOT NULL COMMENT '用户输入',
    response       LONGTEXT     NULL COMMENT 'AI 返回结果',
    token_usage    INT          NULL COMMENT 'Token 消耗量',
    status         VARCHAR(16)  NOT NULL DEFAULT 'SUCCESS' COMMENT '执行状态：SUCCESS/FAILED',
    version        INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    deleted        TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_ai_session_user (user_id),
    CONSTRAINT fk_ai_session_user FOREIGN KEY (user_id) REFERENCES t_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT 'AI 助手使用记录';

-- -------------------------------------------------------------
-- 站点配置
-- -------------------------------------------------------------
CREATE TABLE t_site_setting (
    id          BIGINT PRIMARY KEY COMMENT '主键',
    `key`       VARCHAR(64)  NOT NULL COMMENT '配置键',
    `value`     TEXT         NOT NULL COMMENT '配置值（JSON）',
    description VARCHAR(255) NULL COMMENT '配置描述',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    deleted     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_site_setting_key (`key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '站点配置';

-- -------------------------------------------------------------
-- 额外说明：
--   1. 所有包含 create_time / update_time 的字段可由 MyBatis-Plus MetaObjectHandler 自动填充。
--   2. storage.base-path 下的静态文件（如头像、附件）对应的数据库字段仅保存访问 URL 与存储路径。
--   3. 如果需要额外的演示数据，可在此脚本末尾追加 INSERT 语句。
-- -------------------------------------------------------------
