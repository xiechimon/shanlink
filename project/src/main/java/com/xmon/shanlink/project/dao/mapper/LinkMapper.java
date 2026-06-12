package com.xmon.shanlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xmon.shanlink.project.dao.entity.LinkDO;
import com.xmon.shanlink.project.dto.resp.LinkGroupCountQueryRespDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 短链接持久层
 */
public interface LinkMapper extends BaseMapper<LinkDO> {

    /**
     * 查询分组内短链接数量
     *
     * @param gidList 分组标识集合
     * @return 分组短链接数量集合
     */
    List<LinkGroupCountQueryRespDTO> listGroupShortLinkCount(@Param("gidList") List<String> gidList);

}
