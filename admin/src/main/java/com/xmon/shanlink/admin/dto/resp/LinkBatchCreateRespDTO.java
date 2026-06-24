package com.xmon.shanlink.admin.dto.resp;

import lombok.Data;

import java.util.List;

/**
 * 批量创建短链接响应实体
 */
@Data
public class LinkBatchCreateRespDTO {

    /**
     * 成功创建数量
     */
    private Integer total;

    /**
     * 短链接基础信息集合
     */
    private List<LinkBaseInfoRespDTO> baseLinkInfos;
}
