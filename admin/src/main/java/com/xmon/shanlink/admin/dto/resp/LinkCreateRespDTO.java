package com.xmon.shanlink.admin.dto.resp;

import lombok.Data;

/**
 * 创建短链接响应实体
 */
@Data
public class LinkCreateRespDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}
