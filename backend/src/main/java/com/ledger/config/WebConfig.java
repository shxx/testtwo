package com.ledger.config;

import com.ledger.auth.SessionAuthFilter;
import com.ledger.auth.StateCodec;
import com.ledger.config.FeishuProperties;
import com.ledger.session.SessionService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Web 配置：注册会话过滤器，拦截 /api/**（design.md 第 3 节 AC-1 后端层）。
 */
@Configuration
public class WebConfig {

    @Bean
    public StateCodec stateCodec(FeishuProperties feishuProperties) {
        return new StateCodec(feishuProperties.getStateSecret());
    }

    @Bean
    public FilterRegistrationBean<SessionAuthFilter> sessionAuthFilter(SessionService sessionService) {
        FilterRegistrationBean<SessionAuthFilter> registration =
                new FilterRegistrationBean<>(new SessionAuthFilter(sessionService));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }
}
