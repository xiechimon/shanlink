package com.xmon.shanlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xmon.shanlink.project.dao.entity.LinkDO;
import com.xmon.shanlink.project.dto.req.RecycleBinSaveReqDTO;

/**
 * 回收站管理接口层
 */
public interface RecycleBinService extends IService<LinkDO> {

    /**
     * 保存回收站
     *
     * @param requestParam 请求参数
     */
    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);
}
