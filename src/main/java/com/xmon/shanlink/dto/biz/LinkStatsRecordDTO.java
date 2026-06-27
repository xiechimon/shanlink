package com.xmon.shanlink.dto.biz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 短链接统计实体（MQ 消息载荷）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkStatsRecordDTO implements Serializable {

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 访客标识（UV Cookie 值）
     */
    private String uvFlag;

    /**
     * 是否首次访问（UV）
     */
    private Boolean uvFirstFlag;

    /**
     * 是否首次访问（UIP）
     */
    private Boolean uipFirstFlag;

    /**
     * 访问 IP
     */
    private String remoteAddr;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 访问设备（PC / Mobile）
     */
    private String device;

    /**
     * 访问网络
     */
    private String network;

    /**
     * 访问时间
     */
    private Date currentDate;

    /**
     * 消息唯一标识（用于消费幂等）
     */
    private String keys;
}
