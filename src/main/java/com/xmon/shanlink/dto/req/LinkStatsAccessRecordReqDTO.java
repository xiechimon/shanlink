package com.xmon.shanlink.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 短链接访问记录监控分页请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LinkStatsAccessRecordReqDTO extends Page {

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 启用状态 0：启用 1：未启用
     */
    private Integer enableStatus;
}
