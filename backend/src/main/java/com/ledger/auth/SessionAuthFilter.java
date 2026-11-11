package com.ledger.auth;

import com.ledger.account.Account;
import com.ledger.session.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * 会话过滤器（design.md 第 3 节 AC-1 后端层）：
 * 拦截除登录/回调白名单外的所有 /api/** 请求，无有效会话 Cookie 一律 401，
 * 保证后续新增接口默认受保护，不依赖逐个标注。
 */
public class SessionAuthFilter extends OncePerRequestFilter {

    public static final String ATTR_ACCOUNT = "ledger.account";

    private static final Logger log = LoggerFactory.getLogger(SessionAuthFilter.class);
    private static final Set<String> WHITELIST = Set.of("/api/auth/login", "/api/auth/callback");

    private final SessionService sessionService;

    public SessionAuthFilter(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (WHITELIST.contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        jakarta.servlet.http.Cookie cookie =
                WebUtils.getCookie(request, SessionService.COOKIE_NAME);
        Account account = cookie == null ? null : sessionService.validate(cookie.getValue());
        if (account == null) {
            log.debug("未认证请求被拦截：{}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"unauthorized\"}");
            return;
        }
        request.setAttribute(ATTR_ACCOUNT, account);
        filterChain.doFilter(request, response);
    }
}
