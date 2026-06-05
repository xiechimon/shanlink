package com.xmon.shanlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xmon.shanlink.admin.dao.entity.GroupDO;

/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 新建短链接分组
     * @param name 分组名
     */
    void saveGroup(String name);
}
