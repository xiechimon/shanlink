package com.xmon.shanlink.controller;

import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xmon.shanlink.common.convention.exception.ServiceException;
import com.xmon.shanlink.common.convention.result.Result;
import com.xmon.shanlink.common.convention.result.Results;
import com.xmon.shanlink.dto.req.LinkBatchCreateReqDTO;
import com.xmon.shanlink.dto.req.LinkCreateReqDTO;
import com.xmon.shanlink.dto.req.LinkPageReqDTO;
import com.xmon.shanlink.dto.req.LinkUpdateReqDTO;
import com.xmon.shanlink.dto.resp.LinkBatchCreateExcelVO;
import com.xmon.shanlink.dto.resp.LinkBatchCreateRespDTO;
import com.xmon.shanlink.dto.resp.LinkCreateRespDTO;
import com.xmon.shanlink.dto.resp.LinkGroupCountQueryRespDTO;
import com.xmon.shanlink.dto.resp.LinkPageRespDTO;
import com.xmon.shanlink.service.LinkService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * 批量创建短链接，并将创建结果导出为 Excel
     */
    @PostMapping("/batch")
    public void batchCreateLink(@RequestBody LinkBatchCreateReqDTO requestParam, HttpServletResponse response) {
        LinkBatchCreateRespDTO result = linkService.batchCreateLink(requestParam);
        List<LinkBatchCreateExcelVO> excelData = result.getBaseLinkInfos().stream()
                .map(each -> {
                    LinkBatchCreateExcelVO vo = new LinkBatchCreateExcelVO();
                    vo.setOriginUrl(each.getOriginUrl());
                    vo.setFullShortUrl(each.getFullShortUrl());
                    vo.setDescribe(each.getDescribe());
                    return vo;
                })
                .collect(Collectors.toList());
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("批量创建短链接-" + DateUtil.format(new Date(), "yyyyMMddHHmmss"), StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), LinkBatchCreateExcelVO.class)
                    .sheet("批量创建短链接")
                    .doWrite(excelData);
        } catch (IOException e) {
            throw new ServiceException("批量创建短链接导出失败");
        }
    }

}
