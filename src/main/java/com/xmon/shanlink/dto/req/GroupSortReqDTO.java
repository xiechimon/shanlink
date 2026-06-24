package com.xmon.shanlink.dto.req;

import lombok.Data;

/**
 * 分组排序请求实体
 */
@Data
public class GroupSortReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组排序
     */
    private Integer sortOrder;
}
