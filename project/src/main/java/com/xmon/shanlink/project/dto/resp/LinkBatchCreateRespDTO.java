package com.xmon.shanlink.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量创建短链接响应实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
