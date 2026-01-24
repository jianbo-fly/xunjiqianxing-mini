package com.xunjiqianxing.service.payment.config;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信支付配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wechat-pay")
public class WxPayConfiguration {

    /**
     * 应用ID
     */
    private String appid;

    /**
     * 商户号
     */
    private String mchid;

    /**
     * API V3密钥
     */
    private String apiV3Key;

    /**
     * 证书路径
     */
    private String certPath;

    /**
     * 私钥路径
     */
    private String keyPath;

    /**
     * 回调地址
     */
    private String notifyUrl;

    @Bean
    public WxPayService wxPayService() {
        WxPayConfig config = new WxPayConfig();
        config.setAppId(appid);
        config.setMchId(mchid);
        config.setApiV3Key(apiV3Key);
        config.setPrivateCertPath(certPath);
        config.setPrivateKeyPath(keyPath);
        config.setNotifyUrl(notifyUrl);

        WxPayService wxPayService = new WxPayServiceImpl();
        wxPayService.setConfig(config);
        return wxPayService;
    }
}
