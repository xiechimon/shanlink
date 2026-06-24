package com.xmon.shanlink.admin.controller;

import com.xmon.shanlink.admin.common.convention.result.Result;
import com.xmon.shanlink.admin.common.convention.result.Results;
import com.xmon.shanlink.admin.dto.req.RecycleBinSaveReqDTO;
import com.xmon.shanlink.admin.remote.ShanLinkRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站后管控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shan-link/admin/v1/recycle-bin")
public class RecycleBinController {

    private final ShanLinkRemoteService shanLinkRemoteService;

    /**
     * 移入回收站
     */
    @PostMapping("/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        shanLinkRemoteService.saveRecycleBin(requestParam);
        return Results.success();
    }
}
