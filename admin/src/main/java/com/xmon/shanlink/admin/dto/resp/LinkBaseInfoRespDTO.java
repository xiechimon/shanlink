package com.xmon.shanlink.admin.dto.resp;

import lombok.Data;

/**
 * 短链接基础信息响应实体
 */
@Data
public class LinkBaseInfoRespDTO {

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 短链接描述
     */
    private String describe;
}
