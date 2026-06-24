package com.xmon.shanlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xmon.shanlink.project.dao.entity.LinkDeviceStatsDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 访问设备统计持久层
 */
public interface LinkDeviceStatsMapper extends BaseMapper<LinkDeviceStatsDO> {

    /**
     * 记录设备统计（upsert）
     *
     * @param linkDeviceStatsDO 设备统计实体
     */
    void shortLinkDeviceState(@Param("linkDeviceStats") LinkDeviceStatsDO linkDeviceStatsDO);

    /**
     * 查询设备统计
     *
     * @param fullShortUrl 完整短链接
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 设备统计列表
     */
    List<LinkDeviceStatsDO> listDeviceStatsByShortLink(@Param("fullShortUrl") String fullShortUrl,
                                                       @Param("startDate") String startDate,
                                                       @Param("endDate") String endDate);
}
