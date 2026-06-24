package com.xmon.shanlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xmon.shanlink.project.common.convention.result.Result;
import com.xmon.shanlink.project.common.convention.result.Results;
import com.xmon.shanlink.project.dto.req.RecycleBinPageReqDTO;
import com.xmon.shanlink.project.dto.req.RecycleBinSaveReqDTO;
import com.xmon.shanlink.project.dto.resp.LinkPageRespDTO;
import com.xmon.shanlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站管理控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shan-link/v1/recycle-bin")
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    /**
     * 保存回收站
     */
    @PostMapping("/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        recycleBinService.saveRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 分页查询回收站短链接
     */
    @GetMapping("/page")
    public Result<IPage<LinkPageRespDTO>> pageRecycleBin(RecycleBinPageReqDTO requestParam) {
        return Results.success(recycleBinService.pageRecycleBin(requestParam));
    }
}
