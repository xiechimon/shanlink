package com.xmon.shanlink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xmon.shanlink.admin.common.convention.result.Result;
import com.xmon.shanlink.admin.common.convention.result.Results;
import com.xmon.shanlink.admin.dto.req.LinkCreateReqDTO;
import com.xmon.shanlink.admin.dto.req.LinkPageReqDTO;
import com.xmon.shanlink.admin.dto.req.LinkUpdateReqDTO;
import com.xmon.shanlink.admin.dto.resp.LinkCreateRespDTO;
import com.xmon.shanlink.admin.dto.resp.LinkGroupCountQueryRespDTO;
import com.xmon.shanlink.admin.dto.resp.LinkPageRespDTO;
import com.xmon.shanlink.admin.remote.ShanLinkRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 短链接后管控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shan-link/admin/v1/link")
public class LinkController {

    private static final int CONSOLE_CREATE_TYPE = 1;

    private final ShanLinkRemoteService shortLinkRemoteService;

    /**
     * 创建短链接
     */
    @PostMapping
    public Result<LinkCreateRespDTO> createLink(@RequestBody LinkCreateReqDTO requestParam) {
        requestParam.setCreatedType(CONSOLE_CREATE_TYPE);
        return Results.success(shortLinkRemoteService.createLink(requestParam).getData());
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/page")
    public Result<Page<LinkPageRespDTO>> pageLink(LinkPageReqDTO requestParam) {
        return Results.success(shortLinkRemoteService.pageLink(requestParam).getData());
    }

    /**
     * 修改短链接
     */
    @PutMapping
    public Result<Void> updateLink(@RequestBody LinkUpdateReqDTO requestParam) {
        shortLinkRemoteService.updateLink(requestParam);
        return Results.success();
    }

    /**
     * 删除短链接
     */
    @DeleteMapping
    public Result<Void> deleteLink(@RequestParam("fullShortUrl") String fullShortUrl) {
        shortLinkRemoteService.deleteLink(fullShortUrl);
        return Results.success();
    }

    /**
     * 查询短链接分组内数量
     */
    @GetMapping("/count")
    public Result<List<LinkGroupCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("gidList") List<String> gidList) {
        return Results.success(shortLinkRemoteService.listGroupShortLinkCount(gidList).getData());
    }
}
