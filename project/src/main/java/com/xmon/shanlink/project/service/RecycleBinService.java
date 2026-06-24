package com.xmon.shanlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xmon.shanlink.project.dao.entity.LinkDO;
import com.xmon.shanlink.project.dto.req.RecycleBinPageReqDTO;
import com.xmon.shanlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.xmon.shanlink.project.dto.req.RecycleBinSaveReqDTO;
import com.xmon.shanlink.project.dto.resp.LinkPageRespDTO;

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

    /**
     * 分页查询回收站短链接
     *
     * @param requestParam 请求参数
     */
    IPage<LinkPageRespDTO> pageRecycleBin(RecycleBinPageReqDTO requestParam);

    /**
     * 恢复短链接
     *
     * @param requestParam 请求参数
     */
    void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam);
}
