package com.minimall.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 双登录体系配置：
 * - stpUser  : C 端用户（type = "user"，作用于 /shop/**）
 * - stpAdmin : 后台管理员（type = "admin"，作用于 /admin/**）
 * 二者 token 独立、互不串号。
 *
 * 登录校验采用**注解式**（@SaCheckLogin(type=...)），
 * 登录/注册等放行接口用 @SaIgnore 标注，无需手动维护名单。
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    public static final String TYPE_USER = "user";
    public static final String TYPE_ADMIN = "admin";

    @Bean("stpUser")
    @Primary
    public StpLogic stpUser() {
        return new StpLogic(TYPE_USER);
    }

    @Bean("stpAdmin")
    public StpLogic stpAdmin() {
        return new StpLogic(TYPE_ADMIN);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 开启注解鉴权：无需参数即启用 @SaCheckLogin 等注解解析
        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
    }
}
