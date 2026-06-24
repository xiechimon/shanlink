package com.xmon.shanlink.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接基础信息响应实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
