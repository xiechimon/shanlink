package com.xmon.shanlink.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xmon.shanlink.dao.entity.LinkDO;
import com.xmon.shanlink.dto.req.LinkBatchCreateReqDTO;
import com.xmon.shanlink.dto.req.LinkCreateReqDTO;
import com.xmon.shanlink.dto.req.LinkPageReqDTO;
import com.xmon.shanlink.dto.req.LinkUpdateReqDTO;
import com.xmon.shanlink.dto.resp.LinkBatchCreateRespDTO;
import com.xmon.shanlink.dto.resp.LinkCreateRespDTO;
import com.xmon.shanlink.dto.resp.LinkGroupCountQueryRespDTO;
import com.xmon.shanlink.dto.resp.LinkPageRespDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 短链接接口层
 */
public interface LinkService extends IService<LinkDO> {

    /**
     * 创建短链接
     *
     * @param requestParam 创建短链接请求实体
     * @return 创建短链接响应实体
     */
    LinkCreateRespDTO createLink(LinkCreateReqDTO requestParam);

    /**
     * 批量创建短链接
     *
     * @param requestParam 请求参数
     * @return 批量创建响应实体
     */
    LinkBatchCreateRespDTO batchCreateLink(LinkBatchCreateReqDTO requestParam);

    /**
     * 分页查询短链接
     *
     * @param requestParam 分页查询短链接请求实体
     * @return 短链接分页响应实体
     */
    IPage<LinkPageRespDTO> pageLink(LinkPageReqDTO requestParam);

    /**
     * 修改短链接
     *
     * @param requestParam 修改短链接请求参数
     */
    void updateLink(LinkUpdateReqDTO requestParam);

    /**
     * 删除短链接
     *
     * @param fullShortUrl 完整短链接
     */
    void deleteLink(String fullShortUrl);

    /**
     * 查询短链接分组内数量
     *
     * @param requestParam 查询短链接分组内数量请求参数
     * @return 查询短链接分组内数量响应
     */
    List<LinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam);

    /**
     * 短链接跳转
     *
     * @param shortUri 短链接后缀
     * @param request  HTTP 请求
     * @param response HTTP 响应
     */
    void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response);
}
