package com.xmon.shanlink.admin.remote;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xmon.shanlink.admin.common.convention.result.Result;
import com.xmon.shanlink.admin.common.convention.result.Results;
import com.xmon.shanlink.admin.remote.config.ShanLinkRemoteConfiguration;
import com.xmon.shanlink.admin.dto.req.LinkCreateReqDTO;
import com.xmon.shanlink.admin.dto.req.LinkPageReqDTO;
import com.xmon.shanlink.admin.dto.req.LinkUpdateReqDTO;
import com.xmon.shanlink.admin.dto.req.RecycleBinPageReqDTO;
import com.xmon.shanlink.admin.dto.req.RecycleBinSaveReqDTO;
import com.xmon.shanlink.admin.dto.resp.LinkCreateRespDTO;
import com.xmon.shanlink.admin.dto.resp.LinkGroupCountQueryRespDTO;
import com.xmon.shanlink.admin.dto.resp.LinkPageRespDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 短链接中台远程调用服务
 */
@FeignClient(
        name = "shan-link-remote-service",
        url = "${shan-link.remote.project.url}",
        configuration = ShanLinkRemoteConfiguration.class
)
public interface ShanLinkRemoteService {

    /**
     * 创建短链接
     */
    @PostMapping("/api/shan-link/v1/link")
    Result<LinkCreateRespDTO> createLink(@RequestBody LinkCreateReqDTO requestParam);

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/shan-link/v1/link/page")
    Result<Page<LinkPageRespDTO>> pageLink(@SpringQueryMap LinkPageReqDTO requestParam);

    /**
     * 修改短链接
     */
    @PutMapping("/api/shan-link/v1/link")
    Result<Void> updateLink(@RequestBody LinkUpdateReqDTO requestParam);

    /**
     * 删除短链接
     */
    @DeleteMapping("/api/shan-link/v1/link")
    Result<Void> deleteLink(@RequestParam("fullShortUrl") String fullShortUrl);

    /**
     * 查询短链接分组内数量
     */
    @GetMapping("/api/shan-link/v1/link/count")
    Result<List<LinkGroupCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("gidList") List<String> gidList);

    /**
     * 根据 URL 获取网站标题
     */
    @GetMapping("/api/shan-link/v1/title")
    Result<String> getTitleByUrl(@RequestParam("url") String url);

    /**
     * 移入回收站
     */
    @PostMapping("/api/shan-link/v1/recycle-bin/save")
    Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam);

    /**
     * 分页查询回收站短链接
     */
    @GetMapping("/api/shan-link/v1/recycle-bin/page")
    Result<Page<LinkPageRespDTO>> pageRecycleBin(@SpringQueryMap RecycleBinPageReqDTO requestParam);

}
