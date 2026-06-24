package com.xmon.shanlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xmon.shanlink.dao.entity.LinkNetworkStatsDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 访问网络统计持久层
 */
public interface LinkNetworkStatsMapper extends BaseMapper<LinkNetworkStatsDO> {

    /**
     * 记录网络统计（upsert）
     *
     * @param linkNetworkStatsDO 网络统计实体
     */
    void shortLinkNetworkState(@Param("linkNetworkStats") LinkNetworkStatsDO linkNetworkStatsDO);

    /**
     * 查询网络统计
     *
     * @param fullShortUrl 完整短链接
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 网络统计列表
     */
    List<LinkNetworkStatsDO> listNetworkStatsByShortLink(@Param("fullShortUrl") String fullShortUrl,
                                                         @Param("startDate") String startDate,
                                                         @Param("endDate") String endDate);
}
