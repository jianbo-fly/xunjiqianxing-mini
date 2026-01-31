package com.xunjiqianxing.admin.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 管理后台配置
 */
@Slf4j
@Configuration
public class SaTokenAdminConfig implements WebMvcConfigurer {

    /**
     * 无需登录的白名单
     */
    private static final String[] WHITE_LIST = {
            // 登录接口
            "/admin/auth/login",
            "/admin/auth/initAdmin",
            // API文档
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            // 静态资源
            "/favicon.ico",
            "/error",
            "/admin/category/list",
    };

    /**
     * 仅管理员可访问的接口
     */
    private static final String[] ADMIN_ONLY_LIST = {
            "/admin/supplier/**",
            "/admin/user/**",
            "/admin/member/**",
            "/admin/leader/**",
            "/admin/promoter/**",
            "/admin/banner/**",
            "/admin/category/**",
            "/admin/config/**"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 登录校验
            SaRouter.match("/admin/**")
                    .notMatch(WHITE_LIST)
                    .check(r -> StpUtil.checkLogin());

            // 管理员专属接口校验
            SaRouter.match(ADMIN_ONLY_LIST)
                    .check(r -> {
                        // 检查角色类型
                        String roleType = (String) StpUtil.getSession().get("roleType");
                        if (!"admin".equals(roleType)) {
                            throw new cn.dev33.satoken.exception.NotPermissionException("无权限访问");
                        }
                    });
        }))
        .addPathPatterns("/admin/**")
        .excludePathPatterns(WHITE_LIST);
    }
}
