package com.xmon.shanlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xmon.shanlink.project.dao.entity.LinkStatsTodayDO;
import org.apache.ibatis.annotations.Param;

/**
 * 短链接今日统计持久层
 */
public interface LinkStatsTodayMapper extends BaseMapper<LinkStatsTodayDO> {

    /**
     * 记录今日统计（upsert）
     *
     * @param linkStatsTodayDO 今日统计实体
     */
    void shortLinkTodayState(@Param("linkStatsToday") LinkStatsTodayDO linkStatsTodayDO);
}
