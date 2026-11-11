package com.ledger.auth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * OAuth state 编解码：state = Base64(payload) + "." + Base64(HMAC-SHA256(payload))，
 * payload = 随机串（含毫秒时间戳） + "|" + 原始页面路径。
 * 回调时验签并校验时效（10 分钟），防 CSRF 并还原回跳路径（design.md 第 2/3 节）。
 */
public final class StateCodec {

    private static final long MAX_AGE_MS = 10 * 60 * 1000L;
    private static final char SEPARATOR = '|';

    private final byte[] secret;

    public StateCodec(String stateSecret) {
        if (stateSecret == null || stateSecret.isBlank()) {
            throw new IllegalStateException("feishu.state-secret 未配置");
        }
        this.secret = stateSecret.getBytes(StandardCharsets.UTF_8);
    }

    public String encode(String redirectPath) {
        String nonce = Long.toHexString(System.currentTimeMillis()) + "-" + UUID.randomUUID();
        return encodeRaw(nonce + SEPARATOR + redirectPath);
    }

    /** 校验签名与时效，合法则返回原始页面路径，否则返回 empty。 */
    public Optional<String> decodeAndValidate(String state) {
        if (state == null || state.isBlank()) {
            return Optional.empty();
        }
        int idx = state.indexOf('.');
        if (idx <= 0) {
            return Optional.empty();
        }
        byte[] payload;
        try {
            payload = Base64.getUrlDecoder().decode(state.substring(0, idx));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        String decoded = new String(payload, StandardCharsets.UTF_8);
        String expectedSig = Base64.getUrlEncoder().withoutPadding().encodeToString(hmac(decoded));
        if (!MessageDigest.isEqual(
                expectedSig.getBytes(StandardCharsets.UTF_8),
                state.substring(idx + 1).getBytes(StandardCharsets.UTF_8))) {
            return Optional.empty();
        }
        int sep = decoded.indexOf(SEPARATOR);
        if (sep <= 0 || sep == decoded.length() - 1) {
            return Optional.empty();
        }
        String nonce = decoded.substring(0, sep);
        long issuedAt = parseNonceTimestamp(nonce);
        if (issuedAt < 0 || System.currentTimeMillis() - issuedAt > MAX_AGE_MS) {
            return Optional.empty();
        }
        return Optional.of(decoded.substring(sep + 1));
    }

    /** 仅供测试与编码复用：对给定 payload 签名编码。 */
    String encodeRaw(String payload) {
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encodedPayload + "." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(hmac(payload));
    }

    private long parseNonceTimestamp(String nonce) {
        int dash = nonce.indexOf('-');
        if (dash <= 0) {
            return -1;
        }
        try {
            return Long.parseLong(nonce.substring(0, dash), 16);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private byte[] hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC 计算失败", e);
        }
    }
}
