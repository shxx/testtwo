package com.ledger.ledger;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 台账占位接口（design.md 第 7 节）：受会话过滤器保护，验证 AC-1 后端层防护。
 */
@RestController
public class LedgerPlaceholderController {

    @GetMapping("/api/ledger/placeholder")
    public Map<String, Object> placeholder() {
        return Map.of(
                "status", "placeholder",
                "message", "台账功能建设中");
    }
}
