package com.ledger.session;

import java.time.LocalDateTime;

/**
 * 会话实体（design.md 第 5 节表 sys_session）。
 */
public class Session {

    private String token;
    private Long accountId;
    private LocalDateTime expireAt;
    private LocalDateTime createdAt;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
