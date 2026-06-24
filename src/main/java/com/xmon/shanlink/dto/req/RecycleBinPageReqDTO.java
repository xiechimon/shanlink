package com.xmon.shanlink.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xmon.shanlink.dao.entity.LinkDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 回收站分页请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RecycleBinPageReqDTO extends Page<LinkDO> {

    /**
     * 分组标识
     */
    private List<String> gidList;
}
