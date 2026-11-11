package com.ledger.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.config.FeishuProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 飞书开放平台客户端（OAuth 授权码模式最小实现，design.md 第 2/8 节）：
 * 1. 构建授权页跳转 URL；
 * 2. 授权码换 user_access_token（POST /open-apis/authen/v2/oauth/token）；
 * 3. 获取用户信息取手机号（GET /open-apis/authen/v1/user_info）。
 */
@Component
public class FeishuClient {

    private static final Logger log = LoggerFactory.getLogger(FeishuClient.class);

    private final FeishuProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FeishuClient(FeishuProperties properties) {
        this.properties = properties;
    }

    public String buildAuthorizeUrl(String state) {
        return UriComponentsBuilder.fromHttpUrl(properties.getAuthorizeUrl())
                .queryParam("app_id", properties.getAppId())
                .queryParam("redirect_uri", properties.getRedirectUri())
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    /** 用授权码换取飞书用户手机号；任一步失败抛异常，由调用方拒绝登录。 */
    public String exchangeCodeForMobile(String code) {
        String accessToken = requestUserAccessToken(code);
        return requestMobile(accessToken);
    }

    private String requestUserAccessToken(String code) {
        Map<String, Object> body = new HashMap<>();
        body.put("grant_type", "authorization_code");
        body.put("client_id", properties.getAppId());
        body.put("client_secret", properties.getAppSecret());
        body.put("code", code);
        body.put("redirect_uri", properties.getRedirectUri());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JsonNode resp = parse(postJson(properties.getTokenUrl(), new HttpEntity<>(body, headers)),
                "换取 user_access_token");
        if (resp.hasNonNull("code") && resp.get("code").asInt() != 0) {
            throw new IllegalStateException("飞书换 token 失败 code=" + resp.get("code").asInt());
        }
        String token = resp.path("access_token").asText(null);
        if (!StringUtils.hasText(token)) {
            throw new IllegalStateException("飞书换 token 响应缺少 access_token");
        }
        return token;
    }

    private String requestMobile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        JsonNode resp = parse(restTemplate.exchange(properties.getUserInfoUrl(), HttpMethod.GET,
                        new HttpEntity<>(headers), String.class).getBody(),
                "获取飞书用户信息");
        if (resp.hasNonNull("code") && resp.get("code").asInt() != 0) {
            throw new IllegalStateException("飞书用户信息失败 code=" + resp.get("code").asInt());
        }
        String mobile = resp.path("data").path("mobile").asText(null);
        if (!StringUtils.hasText(mobile)) {
            throw new IllegalStateException("飞书用户信息缺少手机号");
        }
        return mobile;
    }

    private String postJson(String url, HttpEntity<Map<String, Object>> entity) {
        return restTemplate.postForObject(url, entity, String.class);
    }

    private JsonNode parse(String body, String action) {
        if (!StringUtils.hasText(body)) {
            throw new IllegalStateException("飞书接口无响应：" + action);
        }
        try {
            return objectMapper.readTree(body);
        } catch (Exception e) {
            log.error("解析飞书响应失败（{}）：{}", action, body);
            throw new IllegalStateException("飞书响应解析失败：" + action, e);
        }
    }
}
