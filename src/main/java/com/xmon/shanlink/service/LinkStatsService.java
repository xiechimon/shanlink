package com.xmon.shanlink.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xmon.shanlink.dto.biz.LinkStatsRecordDTO;
import com.xmon.shanlink.dto.req.LinkGroupStatsReqDTO;
import com.xmon.shanlink.dto.req.LinkStatsAccessRecordReqDTO;
import com.xmon.shanlink.dto.req.LinkStatsReqDTO;
import com.xmon.shanlink.dto.resp.LinkStatsAccessRecordRespDTO;
import com.xmon.shanlink.dto.resp.LinkStatsRespDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 短链接监控接口层
 */
public interface LinkStatsService {

    /**
     * 记录短链接访问统计
     *
     * @param fullShortUrl 完整短链接
     * @param gid          分组标识
     * @param request      HTTP 请求
     * @param response     HTTP 响应
     */
    void saveStats(String fullShortUrl, String gid, HttpServletRequest request, HttpServletResponse response);

    /**
     * 实际保存短链接访问统计（消费者异步调用）
     *
     * @param requestParam 请求参数
     */
    void actualSaveStats(LinkStatsRecordDTO requestParam);

    /**
     * 获取单个短链接监控数据
     *
     * @param requestParam 请求参数
     * @return 短链接监控数据
     */
    LinkStatsRespDTO oneShortLinkStats(LinkStatsReqDTO requestParam);

    /**
     * 获取分组短链接监控数据
     *
     * @param requestParam 请求参数
     * @return 分组短链接监控数据
     */
    LinkStatsRespDTO groupShortLinkStats(LinkGroupStatsReqDTO requestParam);

    /**
     * 分页查询短链接访问记录
     *
     * @param requestParam 请求参数
     * @return 访问记录分页数据
     */
    IPage<LinkStatsAccessRecordRespDTO> shortLinkAccessRecordPage(LinkStatsAccessRecordReqDTO requestParam);
}
