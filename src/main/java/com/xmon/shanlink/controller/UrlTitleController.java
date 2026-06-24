package com.xmon.shanlink.controller;

import com.xmon.shanlink.common.convention.result.Result;
import com.xmon.shanlink.common.convention.result.Results;
import com.xmon.shanlink.service.UrlTitleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * URL 标题控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shan-link/v1")
public class UrlTitleController {

    private final UrlTitleService urlTitleService;

    /**
     * 根据 URL 获取对应网站的标题
     */
    @GetMapping("/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String url) {
        return Results.success(urlTitleService.getTitleByUrl(url));
    }
}