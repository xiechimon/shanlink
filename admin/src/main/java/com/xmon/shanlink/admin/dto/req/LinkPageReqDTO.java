package com.xmon.shanlink.admin.dto.req;

import lombok.Data;

/**
 * 分页查询短链接请求参数
 */
@Data
public class LinkPageReqDTO {

    /**
     * 当前页
     */
    private Long current;

    /**
     * 每页条数
     */
    private Long size;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 排序标识
     */
    private String orderBy;
}
