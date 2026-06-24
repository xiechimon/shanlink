package com.xmon.shanlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xmon.shanlink.dao.entity.LinkLocaleStatsDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 地区访问统计持久层
 */
public interface LinkLocaleStatsMapper extends BaseMapper<LinkLocaleStatsDO> {

    /**
     * 记录地区统计（upsert）
     *
     * @param linkLocaleStatsDO 地区统计实体
     */
    void shortLinkLocaleState(@Param("linkLocaleStats") LinkLocaleStatsDO linkLocaleStatsDO);

    /**
     * 查询地区统计
     *
     * @param fullShortUrl 完整短链接
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 地区统计列表
     */
    List<LinkLocaleStatsDO> listLocaleStatsByShortLink(@Param("fullShortUrl") String fullShortUrl,
                                                       @Param("startDate") String startDate,
                                                       @Param("endDate") String endDate);
}
