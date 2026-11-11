package com.ledger.session;

import com.ledger.account.Account;
import com.ledger.account.AccountMapper;
import com.ledger.config.LedgerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * 会话签发与校验（design.md 第 4 节）：
 * 有效期由台账系统自控（ledger.session.ttl-hours，默认 8h），
 * 到期即失效重走 SSO，不做滑动续期；登出仅清本系统会话（无单点登出）。
 */
@Service
public class SessionService {

    public static final String COOKIE_NAME = "ledger_session";

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SessionMapper sessionMapper;
    private final AccountMapper accountMapper;
    private final LedgerProperties properties;

    public SessionService(SessionMapper sessionMapper, AccountMapper accountMapper,
                          LedgerProperties properties) {
        this.sessionMapper = sessionMapper;
        this.accountMapper = accountMapper;
        this.properties = properties;
    }

    public String create(long accountId) {
        Session session = new Session();
        session.setToken(randomToken());
        session.setAccountId(accountId);
        LocalDateTime now = LocalDateTime.now();
        session.setCreatedAt(now);
        session.setExpireAt(now.plusHours(properties.getSession().getTtlHours()));
        sessionMapper.insert(session);
        return session.getToken();
    }

    /**
     * 校验会话令牌：存在且未过期、关联账号启用则返回账号；否则清理并返回 null。
     */
    public Account validate(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        Session session = sessionMapper.findByToken(token);
        if (session == null) {
            return null;
        }
        if (session.getExpireAt().isBefore(LocalDateTime.now())) {
            sessionMapper.deleteByToken(token);
            return null;
        }
        Account account = accountMapper.findById(session.getAccountId());
        if (account == null || !account.isEnabled()) {
            log.warn("会话关联账号无效或已停用，accountId={}", session.getAccountId());
            sessionMapper.deleteByToken(token);
            return null;
        }
        return account;
    }

    public void delete(String token) {
        if (token != null && !token.isBlank()) {
            sessionMapper.deleteByToken(token);
        }
    }

    public long ttlSeconds() {
        return properties.getSession().getTtlHours() * 3600L;
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
