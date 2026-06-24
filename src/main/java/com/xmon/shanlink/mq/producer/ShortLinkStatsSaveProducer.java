package com.xmon.shanlink.mq.producer;

import com.xmon.shanlink.dto.biz.LinkStatsRecordDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static com.xmon.shanlink.common.constant.RocketMQConstant.STATS_SAVE_TOPIC;

/**
 * 短链接统计保存消息队列生产者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShortLinkStatsSaveProducer {

    private final RocketMQTemplate rocketMQTemplate;

    /**
     * 发送短链接统计保存消息
     *
     * @param requestParam 短链接统计实体
     */
    public void send(LinkStatsRecordDTO requestParam) {
        Message<LinkStatsRecordDTO> message = MessageBuilder.withPayload(requestParam)
                .setHeader("KEYS", requestParam.getKeys())
                .build();
        rocketMQTemplate.syncSend(STATS_SAVE_TOPIC, message);
    }
}
