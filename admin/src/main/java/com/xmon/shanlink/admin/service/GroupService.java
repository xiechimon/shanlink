package com.xmon.shanlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xmon.shanlink.admin.dao.entity.GroupDO;
import com.xmon.shanlink.admin.dto.req.GroupSortReqDTO;
import com.xmon.shanlink.admin.dto.req.GroupUpdateReqDO;
import com.xmon.shanlink.admin.dto.resp.GroupRespDTO;

import java.util.List;

/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 新建短链接分组
     *
     * @param name 分组名
     */
    void saveGroup(String name);

    /**
     * 查询用户分组集合
     *
     * @return 分组返回实体集合
     */
    List<GroupRespDTO> listGroup();

    /**
     * 更新短链接分组名称
     *
     * @param requestParam 短链接分组请求参数
     */
    void updateGroup(GroupUpdateReqDO requestParam);

    /**
     * 短链接分组排序
     *
     * @param requestParam 短链接分组排序请求参数
     */
    void sortGroup(List<GroupSortReqDTO> requestParam);

    /**
     * 删除短链接分组
     *
     * @param gid 分组标识
     */
    void deleteGroup(String gid);
}
