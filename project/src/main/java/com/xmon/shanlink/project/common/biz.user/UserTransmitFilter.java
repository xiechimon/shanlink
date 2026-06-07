package com.xmon.shanlink.project.common.biz.user;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.xmon.shanlink.project.common.convention.result.Results;
import com.xmon.shanlink.project.common.enums.UserErrorCodeEnum;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.reflect.Type;

import static com.xmon.shanlink.project.common.constant.RedisCacheConstant.USER_LOGIN_KEY;

/**
 * 用户信息传输过滤器
 */
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        String username = httpServletRequest.getHeader("username");
        String token = httpServletRequest.getHeader("token");

        // 公开路径放行（无需登陆）
        String uri = httpServletRequest.getRequestURI();
        boolean isPublicPath = uri.equals("/api/shan-link/admin/v1/user/login") ||
                uri.equals("/api/shan-link/admin/v1/user/register") ||
                uri.equals("/api/shan-link/admin/v1/user/check-username");
        if (!isPublicPath) {
            String userJson = (String) stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username, token);
            if (StrUtil.isBlank(userJson)) {
                httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpServletResponse.setContentType("application/json;charset=UTF-8");
                httpServletResponse.getWriter().write(JSON.toJSONString(Results.failure(UserErrorCodeEnum.USER_NOT_LOGIN.code(), UserErrorCodeEnum.USER_NOT_LOGIN.message())));
                return;
            }
            UserContext.setUser(JSON.parseObject(userJson, (Type) UserInfoDTO.class));
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }
}