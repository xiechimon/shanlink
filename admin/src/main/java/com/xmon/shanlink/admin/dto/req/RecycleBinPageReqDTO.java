package com.xmon.shanlink.admin.dto.req;

import lombok.Data;

import java.util.List;

/**
 * 回收站分页请求参数
 */
@Data
public class RecycleBinPageReqDTO {

    /**
     * 当前页
     */
    private Long current;

    /**
     * 每页条数
     */
    private Long size;

    /**
     * 分组标识列表
     */
    private List<String> gidList;
}
