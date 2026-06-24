package com.xmon.shanlink.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xmon.shanlink.dao.entity.LinkDO;
import com.xmon.shanlink.dto.req.RecycleBinPageReqDTO;
import com.xmon.shanlink.dto.req.RecycleBinRecoverReqDTO;
import com.xmon.shanlink.dto.req.RecycleBinRemoveReqDTO;
import com.xmon.shanlink.dto.req.RecycleBinSaveReqDTO;
import com.xmon.shanlink.dto.resp.LinkPageRespDTO;

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

    /**
     * 移除短链接（彻底删除）
     *
     * @param requestParam 请求参数
     */
    void removeRecycleBin(RecycleBinRemoveReqDTO requestParam);
}
