package com.xmon.shanlink.project.common.constant;

/**
 * 短链接后管 Redis 缓存常量类
 */
public class RedisCacheConstant {

    /**
     * 用户注册分布式锁
     */
    public static final String LOCK_USER_REGISTER_KEY = "shan-link:lock:user-register:";

    /**
     * 分组创建分布式锁
     */
    public static final String LOCK_GROUP_CREATE_KEY = "shan-link:lock:group-create:%s";

    /**
     * 用户登录缓存标识
     */
    public static final String USER_LOGIN_KEY = "shan-link:login:";

    /**
     * 用户登录缓存过期时间（天）
     */
    public static final long USER_LOGIN_TTL = 30L;
}
