package com.xmon.shanlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xmon.shanlink.project.dao.entity.LinkBrowserStatsDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 浏览器访问统计持久层
 */
public interface LinkBrowserStatsMapper extends BaseMapper<LinkBrowserStatsDO> {

    /**
     * 记录浏览器统计（upsert）
     *
     * @param linkBrowserStatsDO 浏览器统计实体
     */
    void shortLinkBrowserState(@Param("linkBrowserStats") LinkBrowserStatsDO linkBrowserStatsDO);

    /**
     * 查询指定日期范围内的浏览器统计
     *
     * @param fullShortUrl 完整短链接
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 浏览器统计列表
     */
    List<LinkBrowserStatsDO> listBrowserStatsByShortLink(@Param("fullShortUrl") String fullShortUrl,
                                                         @Param("startDate") String startDate,
                                                         @Param("endDate") String endDate);
}
