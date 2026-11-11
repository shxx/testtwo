package com.ledger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 台账系统自控配置。
 */
@ConfigurationProperties(prefix = "ledger")
public class LedgerProperties {

    private final Session session = new Session();

    public Session getSession() {
        return session;
    }

    /**
     * 会话配置：有效期由台账系统自控（requirement.md 约束 4）。
     */
    public static class Session {

        /** 会话有效期（小时），默认 8 小时 */
        private int ttlHours = 8;

        public int getTtlHours() {
            return ttlHours;
        }

        public void setTtlHours(int ttlHours) {
            this.ttlHours = ttlHours;
        }
    }
}
