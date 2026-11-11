package com.ledger.auth;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * state 编解码单元测试：签名往返、篡改拒绝、过期拒绝（AC-1/AC-2 state 机制的核心逻辑）。
 */
class StateCodecTest {

    private final StateCodec codec = new StateCodec("test-secret");

    @Test
    void encodeDecodeRoundtrip() {
        String state = codec.encode("/ledger?tab=1");
        Optional<String> decoded = codec.decodeAndValidate(state);
        assertTrue(decoded.isPresent());
        assertEquals("/ledger?tab=1", decoded.get());
    }

    @Test
    void tamperedSignatureRejected() {
        String state = codec.encode("/ledger");
        String tampered = state.substring(0, state.length() - 2) + "xx";
        assertTrue(codec.decodeAndValidate(tampered).isEmpty());
    }

    @Test
    void foreignSecretRejected() {
        StateCodec other = new StateCodec("another-secret");
        assertTrue(codec.decodeAndValidate(other.encode("/ledger")).isEmpty());
    }

    @Test
    void expiredStateRejected() {
        // 构造超过 10 分钟的过期 nonce（时间戳放在 payload 随机串的十六进制毫秒位）
        long tenMinutesAgo = System.currentTimeMillis() - 11 * 60 * 1000L;
        String expiredState = codec.encodeRaw(
                Long.toHexString(tenMinutesAgo) + "-abcd1234|/ledger");
        assertTrue(codec.decodeAndValidate(expiredState).isEmpty());
    }

    @Test
    void malformedStateRejected() {
        assertFalse(codec.decodeAndValidate(null).isPresent());
        assertFalse(codec.decodeAndValidate("").isPresent());
        assertFalse(codec.decodeAndValidate("garbage").isPresent());
    }
}
