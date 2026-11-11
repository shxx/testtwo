package com.ledger.auth;

import com.ledger.account.Account;
import com.ledger.account.AccountMapper;
import com.ledger.config.FeishuProperties;
import com.ledger.session.SessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

/**
 * 认证接口（design.md 第 7 节接口清单）：
 * /api/auth/login    302 至飞书授权页（或 mock 回调）
 * /api/auth/callback 飞书回调：换 token、取手机号、匹配账号、签发会话或拒绝
 * /api/auth/me       校验会话并返回当前用户（由 SessionAuthFilter 保护）
 * /api/auth/logout   清除本系统会话（无单点登出）
 * 不存在任何账号/密码认证接口（AC-3）。
 */
@RestController
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    /** 开发期模拟模式使用的固定 code */
    static final String MOCK_CODE = "mock-code";

    private final StateCodec stateCodec;
    private final FeishuClient feishuClient;
    private final SessionService sessionService;
    private final AccountMapper accountMapper;
    private final FeishuProperties feishuProperties;

    public AuthController(StateCodec stateCodec, FeishuClient feishuClient,
                          SessionService sessionService, AccountMapper accountMapper,
                          FeishuProperties feishuProperties) {
        this.stateCodec = stateCodec;
        this.feishuClient = feishuClient;
        this.sessionService = sessionService;
        this.accountMapper = accountMapper;
        this.feishuProperties = feishuProperties;
    }

    @GetMapping("/api/auth/login")
    public void login(@RequestParam(value = "redirect", required = false) String redirect,
                      HttpServletResponse response) throws IOException {
        String target = sanitizeRedirect(redirect);
        String state = stateCodec.encode(target);
        if (feishuProperties.isMockEnabled()) {
            // 开发期模拟：直接进入回调，走同一套匹配/建会话逻辑（design.md 第 2 节）
            sendRedirect(response, "/api/auth/callback?code=" + MOCK_CODE
                    + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8));
            return;
        }
        sendRedirect(response, feishuClient.buildAuthorizeUrl(state));
    }

    @GetMapping("/api/auth/callback")
    public void callback(@RequestParam(value = "code", required = false) String code,
                         @RequestParam(value = "state", required = false) String state,
                         HttpServletResponse response) throws IOException {
        Optional<String> redirect = stateCodec.decodeAndValidate(state);
        if (redirect.isEmpty()) {
            log.warn("飞书回调 state 校验失败，拒绝登录");
            sendRedirect(response, "/login-denied");
            return;
        }
        try {
            String mobile = resolveMobile(code);
            Account account = accountMapper.findByMobile(mobile);
            if (account == null || !account.isEnabled()) {
                // 手机号匹配不上台账账号（或已停用）→ 拒绝（AC-3）
                log.info("飞书用户手机号未匹配到启用的台账账号，拒绝登录");
                sendRedirect(response, "/login-denied");
                return;
            }
            String token = sessionService.create(account.getId());
            Cookie cookie = new Cookie(SessionService.COOKIE_NAME, token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge((int) sessionService.ttlSeconds());
            cookie.setAttribute("SameSite", "Lax");
            response.addCookie(cookie);
            // 认证成功 → 相对路径回跳原始页面（AC-2），保持浏览器停留在前端源站
            sendRedirect(response, redirect.get());
        } catch (Exception e) {
            log.error("飞书回调处理失败：{}", e.getMessage());
            sendRedirect(response, "/login-denied");
        }
    }

    /**
     * 以相对路径 302 重定向（HttpServletResponse.sendRedirect 会将相对路径绝对化为
     * 后端端口，导致浏览器绕过前端代理，因此显式写 Location 头保留相对路径）。
     */
    private void sendRedirect(HttpServletResponse response, String location) {
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader("Location", location);
    }

    @GetMapping("/api/auth/me")
    public Map<String, Object> me(HttpServletRequest request) {
        Account account = (Account) request.getAttribute(SessionAuthFilter.ATTR_ACCOUNT);
        return Map.of(
                "name", account.getName(),
                "mobile", account.getMobile());
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (SessionService.COOKIE_NAME.equals(c.getName())) {
                    sessionService.delete(c.getValue());
                }
            }
        }
        Cookie clear = new Cookie(SessionService.COOKIE_NAME, "");
        clear.setHttpOnly(true);
        clear.setPath("/");
        clear.setMaxAge(0);
        response.addCookie(clear);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private String resolveMobile(String code) {
        if (feishuProperties.isMockEnabled() && MOCK_CODE.equals(code)) {
            return feishuProperties.getMockMobile();
        }
        if (!StringUtils.hasText(code)) {
            throw new IllegalArgumentException("回调缺少 code");
        }
        return feishuClient.exchangeCodeForMobile(code);
    }

    /** 仅允许站内相对路径回跳，防开放重定向。 */
    private String sanitizeRedirect(String redirect) {
        if (redirect == null || redirect.isBlank() || !redirect.startsWith("/")
                || redirect.startsWith("//") || redirect.contains("\r") || redirect.contains("\n")) {
            return "/";
        }
        return redirect;
    }
}
