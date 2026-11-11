package com.ledger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 飞书 SSO 接入配置。凭证与回调域名通过环境变量注入（遗留事项，见 requirement.md 第 6 节）。
 */
@ConfigurationProperties(prefix = "feishu")
public class FeishuProperties {

    /** 飞书企业自建应用 App ID */
    private String appId;

    /** 飞书企业自建应用 App Secret */
    private String appSecret;

    /** OAuth 回调地址（须与部署域名一致） */
    private String redirectUri;

    /** state 签名密钥（生产环境必须通过环境变量覆盖默认值） */
    private String stateSecret;

    /** 开发期模拟开关：凭证未到位时以本地模拟用户走通登录链路（默认关闭） */
    private boolean mockEnabled = false;

    /** 模拟模式使用的台账账号手机号（须在 data.sql 中预置才能匹配成功） */
    private String mockMobile = "13800000000";

    private String authorizeUrl = "https://open.feishu.cn/open-apis/authen/v1/authorize";
    private String tokenUrl = "https://open.feishu.cn/open-apis/authen/v2/oauth/token";
    private String userInfoUrl = "https://open.feishu.cn/open-apis/authen/v1/user_info";

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getStateSecret() {
        return stateSecret;
    }

    public void setStateSecret(String stateSecret) {
        this.stateSecret = stateSecret;
    }

    public boolean isMockEnabled() {
        return mockEnabled;
    }

    public void setMockEnabled(boolean mockEnabled) {
        this.mockEnabled = mockEnabled;
    }

    public String getMockMobile() {
        return mockMobile;
    }

    public void setMockMobile(String mockMobile) {
        this.mockMobile = mockMobile;
    }

    public String getAuthorizeUrl() {
        return authorizeUrl;
    }

    public void setAuthorizeUrl(String authorizeUrl) {
        this.authorizeUrl = authorizeUrl;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getUserInfoUrl() {
        return userInfoUrl;
    }

    public void setUserInfoUrl(String userInfoUrl) {
        this.userInfoUrl = userInfoUrl;
    }
}
