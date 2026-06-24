package com.xmon.shanlink.mq.consumer;

import com.xmon.shanlink.common.convention.exception.ServiceException;
import com.xmon.shanlink.dto.biz.LinkStatsRecordDTO;
import com.xmon.shanlink.mq.idempotent.MessageQueueIdempotentHandler;
import com.xmon.shanlink.service.LinkStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import static com.xmon.shanlink.common.constant.RocketMQConstant.STATS_SAVE_CONSUMER_GROUP;
import static com.xmon.shanlink.common.constant.RocketMQConstant.STATS_SAVE_TOPIC;

/**
 * 短链接统计保存消息队列消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = STATS_SAVE_TOPIC,
        consumerGroup = STATS_SAVE_CONSUMER_GROUP
)
public class ShortLinkStatsSaveConsumer implements RocketMQListener<LinkStatsRecordDTO> {

    private final LinkStatsService linkStatsService;
    private final MessageQueueIdempotentHandler messageQueueIdempotentHandler;

    @Override
    public void onMessage(LinkStatsRecordDTO message) {
        String messageId = message.getKeys();
        // 幂等校验：判断消息是否已被消费或正在消费
        if (!messageQueueIdempotentHandler.isMessageBeingConsumed(messageId)) {
            // 已完成则直接返回，未完成说明仍在处理中，抛异常触发重试
            if (messageQueueIdempotentHandler.isAccomplish(messageId)) {
                return;
            }
            throw new ServiceException("消息未完成流程，需要消息队列重试");
        }
        try {
            linkStatsService.actualSaveStats(message);
        } catch (Throwable ex) {
            // 消费异常删除标识，便于消息重试
            messageQueueIdempotentHandler.delMessageProcessed(messageId);
            log.error("记录短链接监控消费异常", ex);
            throw ex;
        }
        messageQueueIdempotentHandler.setAccomplish(messageId);
    }
}
