package com.xmon.shanlink.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xmon.shanlink.common.convention.result.Result;
import com.xmon.shanlink.common.convention.result.Results;
import com.xmon.shanlink.dto.req.LinkGroupStatsAccessRecordReqDTO;
import com.xmon.shanlink.dto.req.LinkGroupStatsReqDTO;
import com.xmon.shanlink.dto.req.LinkStatsAccessRecordReqDTO;
import com.xmon.shanlink.dto.req.LinkStatsReqDTO;
import com.xmon.shanlink.dto.resp.LinkStatsAccessRecordRespDTO;
import com.xmon.shanlink.dto.resp.LinkStatsRespDTO;
import com.xmon.shanlink.service.LinkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shan-link/v1/stats")
public class LinkStatsController {

    private final LinkStatsService linkStatsService;

    /**
     * 查询单个短链接监控数据
     */
    @GetMapping
    public Result<LinkStatsRespDTO> oneShortLinkStats(LinkStatsReqDTO requestParam) {
        return Results.success(linkStatsService.oneShortLinkStats(requestParam));
    }

    /**
     * 查询分组短链接监控数据
     */
    @GetMapping("/group")
    public Result<LinkStatsRespDTO> groupShortLinkStats(LinkGroupStatsReqDTO requestParam) {
        return Results.success(linkStatsService.groupShortLinkStats(requestParam));
    }

    /**
     * 分页查询短链接访问记录
     */
    @GetMapping("/access-record")
    public Result<IPage<LinkStatsAccessRecordRespDTO>> shortLinkAccessRecordPage(LinkStatsAccessRecordReqDTO requestParam) {
        return Results.success(linkStatsService.shortLinkAccessRecordPage(requestParam));
    }

    /**
     * 分页查询分组短链接访问记录
     */
    @GetMapping("/access-record/group")
    public Result<IPage<LinkStatsAccessRecordRespDTO>> groupShortLinkAccessRecordPage(LinkGroupStatsAccessRecordReqDTO requestParam) {
        return Results.success(linkStatsService.groupShortLinkAccessRecordPage(requestParam));
    }
}
