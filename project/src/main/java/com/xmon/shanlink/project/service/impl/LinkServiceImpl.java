package com.xmon.shanlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmon.shanlink.project.common.convention.exception.ServiceException;
import com.xmon.shanlink.project.dao.entity.LinkDO;
import com.xmon.shanlink.project.dao.mapper.LinkMapper;
import com.xmon.shanlink.project.dto.req.LinkCreateReqDTO;
import com.xmon.shanlink.project.dto.req.LinkPageReqDTO;
import com.xmon.shanlink.project.dto.req.LinkUpdateReqDTO;
import com.xmon.shanlink.project.dto.resp.LinkCreateRespDTO;
import com.xmon.shanlink.project.dto.resp.LinkPageRespDTO;
import com.xmon.shanlink.project.service.LinkService;
import com.xmon.shanlink.project.toolkit.HashUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 短链接接口实现层
 */
@Service
@RequiredArgsConstructor
public class LinkServiceImpl extends ServiceImpl<LinkMapper, LinkDO> implements LinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    @Value("${shan-link.domain.default}")
    private String createShortLinkDefaultDomain;

    @Override
    public LinkCreateRespDTO createLink(LinkCreateReqDTO requestParam) {
        // 生成短链接 URI
        String shortUri = generateShortUri(requestParam.getOriginUrl());
        String fullShortUrl = StrBuilder.create(createShortLinkDefaultDomain)
                .append("/")
                .append(shortUri)
                .toString();
        // 构建 LinkDO 对象并保存
        LinkDO linkDO = LinkDO.builder()
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .createdType(requestParam.getCreatedType())
                .domain(requestParam.getDomain())
                .clickNum(0)
                .enableStatus(0)
                .totalPv(0)
                .totalUv(0)
                .totalUip(0)
                .delTime(0L)
                .shortUri(shortUri)
                .fullShortUrl(fullShortUrl)
                .build();
        try {
            save(linkDO);
        } catch (DuplicateKeyException e) {
            // 判断是否存在 BF，不存在则添加
            if (!shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl)) {
                shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
            }
            throw new ServiceException(String.format("短链接：%s 已存在，请稍后再试", fullShortUrl));
        }
        // 新建短链接添加到 BF，防止缓存穿透
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        // 返回创建结果
        return BeanUtil.toBean(linkDO, LinkCreateRespDTO.class);
    }

    @Override
    public IPage<LinkPageRespDTO> pageLink(LinkPageReqDTO requestParam) {
        LambdaQueryWrapper<LinkDO> queryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                .eq(LinkDO::getGid, requestParam.getGid())
                .eq(LinkDO::getEnableStatus, 0)
                .eq(LinkDO::getDelFlag, 0)
                .eq(LinkDO::getDelTime, 0L)
                .orderByDesc(LinkDO::getCreateTime);

        IPage<LinkDO> page = page(requestParam, queryWrapper);
        return page.convert(each -> BeanUtil.toBean(each, LinkPageRespDTO.class));
    }

    @Override
    public void updateLink(LinkUpdateReqDTO requestParam) {
        update(Wrappers.lambdaUpdate(LinkDO.class)
                       .set(LinkDO::getOriginUrl, requestParam.getOriginUrl())
                       .set(LinkDO::getValidDateType, requestParam.getValidDateType())
                       .set(LinkDO::getValidDate, requestParam.getValidDate())
                       .set(LinkDO::getDescribe, requestParam.getDescribe())
                       .eq(LinkDO::getGid, requestParam.getGid())
                       .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                       .eq(LinkDO::getDelFlag, 0)
                       .eq(LinkDO::getDelTime, 0L));
    }

    @Override
    public void deleteLink(String fullShortUrl) {
        update(Wrappers.lambdaUpdate(LinkDO.class)
                       .set(LinkDO::getDelFlag, 1)
                       .set(LinkDO::getDelTime, System.currentTimeMillis())
                       .eq(LinkDO::getFullShortUrl, fullShortUrl)
                       .eq(LinkDO::getDelFlag, 0)
                       .eq(LinkDO::getDelTime, 0L));
    }

    /**
     * 短链接 URI 生成
     */
    private String generateShortUri(String originUrl) {
        int customGenerateCount = 0;
        String shortUri;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接频繁生成，请稍后再试");
            }
            String seed = originUrl + UUID.randomUUID();
            shortUri = HashUtil.hashToBase62(seed);
            if (!shortUriCreateCachePenetrationBloomFilter.contains(createShortLinkDefaultDomain + "/" + shortUri)) {
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }
}
