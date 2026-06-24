package com.xmon.shanlink.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xmon.shanlink.dao.entity.LinkDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 短链接分页请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LinkPageReqDTO extends Page<LinkDO> {

    /**
     * 分组标识
     */
    private String gid;


    /**
     * 排序标识
     */
    private String orderBy;
}

