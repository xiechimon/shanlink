package com.xmon.shanlink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xmon.shanlink.admin.common.convention.result.Result;
import com.xmon.shanlink.admin.common.convention.result.Results;
import com.xmon.shanlink.admin.dto.req.RecycleBinPageReqDTO;
import com.xmon.shanlink.admin.dto.req.RecycleBinSaveReqDTO;
import com.xmon.shanlink.admin.dto.resp.GroupRespDTO;
import com.xmon.shanlink.admin.dto.resp.LinkPageRespDTO;
import com.xmon.shanlink.admin.remote.ShanLinkRemoteService;
import com.xmon.shanlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 回收站后管控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shan-link/admin/v1/recycle-bin")
public class RecycleBinController {

    private final ShanLinkRemoteService shanLinkRemoteService;
    private final GroupService groupService;

    /**
     * 移入回收站
     */
    @PostMapping("/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        shanLinkRemoteService.saveRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 分页查询回收站短链接
     */
    @GetMapping("/page")
    public Result<Page<LinkPageRespDTO>> pageRecycleBin(RecycleBinPageReqDTO requestParam) {
        List<String> gidList = groupService.listGroup().stream()
                .map(GroupRespDTO::getGid)
                .toList();
        requestParam.setGidList(gidList);
        return Results.success(shanLinkRemoteService.pageRecycleBin(requestParam).getData());
    }
}
