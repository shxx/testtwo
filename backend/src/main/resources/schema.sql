-- 门店网络台账：建表脚本（幂等，可重复执行）
-- design.md 第 5 节数据模型

CREATE TABLE IF NOT EXISTS ledger_account (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '账号 ID',
    name       VARCHAR(64)  NOT NULL COMMENT '姓名',
    mobile     VARCHAR(20)  NOT NULL COMMENT '手机号（SSO 匹配唯一依据）',
    status     TINYINT      NOT NULL DEFAULT 1 COMMENT '1 启用 / 0 停用',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ledger_account_mobile (mobile)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '台账账号表（无密码字段）';

CREATE TABLE IF NOT EXISTS sys_session (
    token      VARCHAR(64)  NOT NULL COMMENT '随机会话令牌（Cookie 值）',
    account_id BIGINT       NOT NULL COMMENT '关联台账账号',
    expire_at  DATETIME     NOT NULL COMMENT '过期时间（台账自控）',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '签发时间',
    PRIMARY KEY (token),
    KEY idx_sys_session_expire (expire_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '会话表';
