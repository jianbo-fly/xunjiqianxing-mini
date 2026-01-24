package com.xunjiqianxing.app.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置
 */
@Slf4j
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 无需登录的白名单
     */
    private static final String[] WHITE_LIST = {
            "/api/user/login",
            "/api/user/loginByCode",
            "/api/user/devLogin",
            "/api/home/**",
            "/api/route/list",
            "/api/route/*",
            "/api/route/*/packages",
            "/api/route/package/*",
            "/api/route/package/*/calendar",
            "/api/member/benefits",
            "/api/promoter/bindByCode",
            "/notify/**"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 调试日志
            String tokenValue = StpUtil.getTokenValue();
        
            
            // 强制校验登录
            StpUtil.checkLogin();
        }))
        .addPathPatterns("/api/**")
        .excludePathPatterns(WHITE_LIST);
    }
}
