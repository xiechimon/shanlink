package com.xmon.shanlink.admin.remote.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 短链接远程调用配置
 */
@Configuration
public class ShanLinkRemoteConfiguration {

    /**
     * 透传鉴权请求头，兼容 project 服务登录校验
     */
    @Bean
    public RequestInterceptor shortLinkRemoteRequestInterceptor() {
        return requestTemplate -> {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
                return;
            }
            HttpServletRequest request = servletRequestAttributes.getRequest();
            String username = request.getHeader("username");
            String token = request.getHeader("token");
            if (username != null) {
                requestTemplate.header("username", username);
            }
            if (token != null) {
                requestTemplate.header("token", token);
            }
        };
    }
}
