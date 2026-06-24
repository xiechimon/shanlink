package com.xmon.shanlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xmon.shanlink.dao.entity.LinkAccessStatsDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 短链接访问统计持久层
 */
public interface LinkAccessStatsMapper extends BaseMapper<LinkAccessStatsDO> {

    /**
     * 记录访问统计（upsert）
     *
     * @param linkAccessStatsDO 访问统计实体
     */
    void shortLinkStats(@Param("linkAccessStats") LinkAccessStatsDO linkAccessStatsDO);

    /**
     * 查询指定日期范围内的访问统计
     *
     * @param fullShortUrl 完整短链接
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 访问统计列表
     */
    List<LinkAccessStatsDO> listStatsByShortLink(@Param("fullShortUrl") String fullShortUrl,
                                                 @Param("startDate") String startDate,
                                                 @Param("endDate") String endDate);
}
