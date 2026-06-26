package com.xmon.shanlink.common.constant;

/**
 * Redis Key 常量类
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

    /**
     * 短链接跳转前缀 Key
     */
    public static final String GOTO_SHORT_LINK_KEY = "shan-link:goto:%s";

    /**
     * 短链接空值跳转前缀 Key
     */
    public static final String GOTO_IS_NULL_SHORT_LINK_KEY = "shan-link:is-null:goto_%s";

    /**
     * 短链接空值缓存过期时间（秒）
     */
    public static final long GOTO_IS_NULL_SHORT_LINK_EXPIRE_SECONDS = 30 * 60L;

    /**
     * 短链接跳转锁前缀 Key
     */
    public static final String LOCK_GOTO_SHORT_LINK_KEY = "shan-link:lock:goto:%s";

    /**
     * 短链接修改分组 ID 锁前缀 Key
     */
    public static final String LOCK_GID_UPDATE_KEY = "shan-link:lock:update-gid:%s";

    /**
     * 短链接延迟队列消费统计 Key
     */
    public static final String DELAY_QUEUE_STATS_KEY = "shan-link:delay-queue:stats";

    /**
     * 短链接统计判断是否新用户缓存标识
     */
    public static final String SHORT_LINK_STATS_UV_KEY = "shan-link:stats:uv:";

    /**
     * 短链接统计判断是否新 IP 缓存标识
     */
    public static final String SHORT_LINK_STATS_UIP_KEY = "shan-link:stats:uip:";

    /**
     * 短链接监控消息保存队列 Topic 缓存标识
     */
    public static final String SHORT_LINK_STATS_STREAM_TOPIC_KEY = "shan-link:stats-stream";

    /**
     * 短链接监控消息保存队列 Group 缓存标识
     */
    public static final String SHORT_LINK_STATS_STREAM_GROUP_KEY = "shan-link:stats-stream:only-group";

    /**
     * 创建短链接锁标识
     */
    public static final String SHORT_LINK_CREATE_LOCK_KEY = "shan-link:lock:create";

    /**
     * 短链接统计消息消费幂等标识
     */
    public static final String STATS_IDEMPOTENT_KEY = "shan-link:stats:idempotent:%s";

    /**
     * 防止短链接创建缓存穿透布隆过滤器 Key
     */
    public static final String BF_SHORT_URI_CREATE_KEY = "shan-link:bf:short-uri-create";

    /**
     * 防止用户注册缓存穿透布隆过滤器 Key
     */
    public static final String BF_USER_REGISTER_KEY = "shan-link:bf:user-register";

    /**
     * 防止分组标识注册缓存穿透布隆过滤器 Key
     */
    public static final String BF_GID_REGISTER_KEY = "shan-link:bf:gid-register";
}
