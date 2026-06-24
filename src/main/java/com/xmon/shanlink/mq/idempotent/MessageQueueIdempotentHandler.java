package com.xmon.shanlink.mq.idempotent;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.xmon.shanlink.common.constant.RedisCacheConstant.STATS_IDEMPOTENT_KEY;

/**
 * 消息队列幂等处理器
 */
@Component
@RequiredArgsConstructor
public class MessageQueueIdempotentHandler {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 判断当前消息是否正在被消费
     *
     * @param messageId 消息唯一标识
     * @return 是否首次消费（true 表示首次，可继续消费流程）
     */
    public boolean isMessageBeingConsumed(String messageId) {
        String key = String.format(STATS_IDEMPOTENT_KEY, messageId);
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, "0", 2, TimeUnit.MINUTES));
    }

    /**
     * 判断消息消费流程是否已执行完成
     *
     * @param messageId 消息唯一标识
     * @return 是否已完成
     */
    public boolean isAccomplish(String messageId) {
        String key = String.format(STATS_IDEMPOTENT_KEY, messageId);
        return Objects.equals(stringRedisTemplate.opsForValue().get(key), "1");
    }

    /**
     * 标记消息消费流程已完成
     *
     * @param messageId 消息唯一标识
     */
    public void setAccomplish(String messageId) {
        String key = String.format(STATS_IDEMPOTENT_KEY, messageId);
        stringRedisTemplate.opsForValue().set(key, "1", 2, TimeUnit.MINUTES);
    }

    /**
     * 删除消费标识（消费异常时调用，便于消息重试）
     *
     * @param messageId 消息唯一标识
     */
    public void delMessageProcessed(String messageId) {
        String key = String.format(STATS_IDEMPOTENT_KEY, messageId);
        stringRedisTemplate.delete(key);
    }
}
