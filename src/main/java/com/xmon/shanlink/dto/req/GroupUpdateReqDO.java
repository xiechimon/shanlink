package com.xmon.shanlink.dto.req;

import com.xmon.shanlink.common.database.BaseDO;
import lombok.*;

/**
 * 分组更新请求实体
 */
@Data
public class GroupUpdateReqDO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名称
     */
    private String name;

}
