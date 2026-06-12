package com.xmon.shanlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xmon.shanlink.project.common.convention.result.Result;
import com.xmon.shanlink.project.common.convention.result.Results;
import com.xmon.shanlink.project.dto.req.LinkCreateReqDTO;
import com.xmon.shanlink.project.dto.req.LinkPageReqDTO;
import com.xmon.shanlink.project.dto.req.LinkUpdateReqDTO;
import com.xmon.shanlink.project.dto.resp.LinkCreateRespDTO;
import com.xmon.shanlink.project.dto.resp.LinkGroupCountQueryRespDTO;
import com.xmon.shanlink.project.dto.resp.LinkPageRespDTO;
import com.xmon.shanlink.project.service.LinkService;
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
 * 短链接控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shan-link/v1/link")
public class LinkController {

    private final LinkService linkService;

    /**
     * 创建短链接
     */
    @PostMapping
    public Result<LinkCreateRespDTO> createLink(@RequestBody LinkCreateReqDTO requestParam) {
        return Results.success(linkService.createLink(requestParam));
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/page")
    public Result<IPage<LinkPageRespDTO>> pageLink(LinkPageReqDTO requestParam) {
        return Results.success(linkService.pageLink(requestParam));
    }

    /**
     * 修改短链接
     */
    @PutMapping
    public Result<Void> updateLink(@RequestBody LinkUpdateReqDTO requestParam) {
        linkService.updateLink(requestParam);
        return Results.success();
    }

    /**
     * 删除短链接
     */
    @DeleteMapping
    public Result<Void> deleteLink(@RequestParam("fullShortUrl") String fullShortUrl) {
        linkService.deleteLink(fullShortUrl);
        return Results.success();
    }

    /**
     * 查询短链接分组内数量
     */
    @GetMapping("/count")
    public Result<List<LinkGroupCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("gidList") List<String> gidList) {
        return Results.success(linkService.listGroupShortLinkCount(gidList));
    }
}
