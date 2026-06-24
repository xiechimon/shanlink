package com.xmon.shanlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xmon.shanlink.admin.common.convention.result.Result;
import com.xmon.shanlink.admin.common.convention.result.Results;
import com.xmon.shanlink.admin.dto.req.LinkGroupStatsReqDTO;
import com.xmon.shanlink.admin.dto.req.LinkStatsAccessRecordReqDTO;
import com.xmon.shanlink.admin.dto.req.LinkStatsReqDTO;
import com.xmon.shanlink.admin.dto.resp.LinkStatsAccessRecordRespDTO;
import com.xmon.shanlink.admin.dto.resp.LinkStatsRespDTO;
import com.xmon.shanlink.admin.remote.ShanLinkRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控后管控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shan-link/admin/v1/stats")
public class LinkStatsController {

    private final ShanLinkRemoteService shortLinkRemoteService;

    /**
     * 查询单个短链接监控数据
     */
    @GetMapping
    public Result<LinkStatsRespDTO> oneShortLinkStats(LinkStatsReqDTO requestParam) {
        return Results.success(shortLinkRemoteService.oneShortLinkStats(requestParam).getData());
    }

    /**
     * 查询分组短链接监控数据
     */
    @GetMapping("/group")
    public Result<LinkStatsRespDTO> groupShortLinkStats(LinkGroupStatsReqDTO requestParam) {
        return Results.success(shortLinkRemoteService.groupShortLinkStats(requestParam).getData());
    }

    /**
     * 分页查询短链接访问记录
     */
    @GetMapping("/access-record")
    public Result<IPage<LinkStatsAccessRecordRespDTO>> shortLinkAccessRecordPage(LinkStatsAccessRecordReqDTO requestParam) {
        return Results.success(shortLinkRemoteService.shortLinkAccessRecordPage(requestParam).getData());
    }
}
