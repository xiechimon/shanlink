package com.xmon.shanlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xmon.shanlink.dao.entity.LinkOsStatsDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作系统访问统计持久层
 */
public interface LinkOsStatsMapper extends BaseMapper<LinkOsStatsDO> {

    /**
     * 记录操作系统统计（upsert）
     *
     * @param linkOsStatsDO 操作系统统计实体
     */
    void shortLinkOsState(@Param("linkOsStats") LinkOsStatsDO linkOsStatsDO);

    /**
     * 查询操作系统统计
     *
     * @param fullShortUrl 完整短链接
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 操作系统统计列表
     */
    List<LinkOsStatsDO> listOsStatsByShortLink(@Param("fullShortUrl") String fullShortUrl,
                                               @Param("startDate") String startDate,
                                               @Param("endDate") String endDate);
}
