package com.xmon.shanlink.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmon.shanlink.common.convention.exception.ClientException;
import com.xmon.shanlink.common.convention.exception.ServiceException;
import com.xmon.shanlink.common.enums.LinkErrorCodeEnum;
import com.xmon.shanlink.common.enums.VailDateTypeEnum;
import com.xmon.shanlink.dao.entity.LinkGotoDO;
import com.xmon.shanlink.dao.mapper.LinkGotoMapper;
import com.xmon.shanlink.dto.resp.*;
import com.xmon.shanlink.toolkit.LinkUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Objects;

import com.xmon.shanlink.dao.entity.LinkDO;
import com.xmon.shanlink.dao.mapper.LinkMapper;
import com.xmon.shanlink.dto.req.LinkBatchCreateReqDTO;
import com.xmon.shanlink.dto.req.LinkCreateReqDTO;
import com.xmon.shanlink.dto.req.LinkPageReqDTO;
import com.xmon.shanlink.dto.req.LinkUpdateReqDTO;
import com.xmon.shanlink.service.LinkService;
import com.xmon.shanlink.service.LinkStatsService;
import com.xmon.shanlink.toolkit.HashUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.xmon.shanlink.common.constant.RedisCacheConstant.*;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LinkServiceImpl extends ServiceImpl<LinkMapper, LinkDO> implements LinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final LinkMapper linkMapper;
    private final LinkGotoMapper linkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final LinkStatsService linkStatsService;

    @Value("${shan-link.domain.default}")
    private String createShortLinkDefaultDomain;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public LinkCreateRespDTO createLink(LinkCreateReqDTO requestParam) {
        // 生成短链接 URI
        String shortUri = generateShortUri(requestParam.getOriginUrl());
        String fullShortUrl = StrBuilder.create(createShortLinkDefaultDomain)
                .append("/")
                .append(shortUri)
                .toString();

        // 构建 LinkDO & LinkGotoDO
        LinkDO linkDO = LinkDO.builder()
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .createdType(requestParam.getCreatedType())
                .domain(createShortLinkDefaultDomain)
                .clickNum(0)
                .enableStatus(0)
                .totalPv(0)
                .totalUv(0)
                .totalUip(0)
                .delTime(0L)
                .shortUri(shortUri)
                .fullShortUrl(fullShortUrl)
                .favicon(getFavicon(requestParam.getOriginUrl()))
                .build();
        LinkGotoDO linkGotoDO = LinkGotoDO.builder()
                .gid(requestParam.getGid())
                .fullShortUrl(fullShortUrl)
                .build();
        try {
            // 保存到 DB
            save(linkDO);
            linkGotoMapper.insert(linkGotoDO);
        } catch (DuplicateKeyException e) {
            // 判断是否存在 BF，不存在则添加
            if (!shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl)) {
                shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
            }
            throw new ServiceException(String.format("短链接：%s 已存在，请稍后再试", fullShortUrl));
        }

        // 缓存预热
        stringRedisTemplate.opsForValue().set(
                String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                requestParam.getOriginUrl(),
                LinkUtil.getLinkCacheValidTime(requestParam.getValidDate()), TimeUnit.MILLISECONDS
        );

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
        return page.convert(each -> {
            LinkPageRespDTO dto = BeanUtil.toBean(each, LinkPageRespDTO.class);
            dto.setTodayPv(0);
            dto.setTodayUv(0);
            dto.setTodayUip(0);
            return dto;
        });
    }

    @Override
    public LinkBatchCreateRespDTO batchCreateLink(LinkBatchCreateReqDTO requestParam) {
        List<String> originUrls = requestParam.getOriginUrls();
        List<String> describes = requestParam.getDescribes();
        List<LinkBaseInfoRespDTO> results = new ArrayList<>();

        for (int i = 0; i < originUrls.size(); i++) {
            String describe = describes != null && i < describes.size() ? describes.get(i) : null;
            LinkCreateReqDTO createReq = BeanUtil.toBean(requestParam, LinkCreateReqDTO.class);
            createReq.setOriginUrl(originUrls.get(i));
            createReq.setDescribe(describe);
            try {
                LinkCreateRespDTO shortLink = createLink(createReq);
                results.add(LinkBaseInfoRespDTO.builder()
                        .originUrl(shortLink.getOriginUrl())
                        .fullShortUrl(shortLink.getFullShortUrl())
                        .describe(describe)
                        .build());
            } catch (Throwable ex) {
                log.error("批量创建短链接失败，原始链接：{}，异常信息：{}", originUrls.get(i), ex.getMessage());
            }
        }

        return LinkBatchCreateRespDTO.builder()
                .total(results.size())
                .baseLinkInfos(results)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateLink(LinkUpdateReqDTO requestParam) {
        // 判断原始短链是否存在
        LinkDO existing = getOne(Wrappers.lambdaQuery(LinkDO.class)
                .eq(LinkDO::getGid, requestParam.getOriginGid())
                .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(LinkDO::getDelFlag, 0)
                .eq(LinkDO::getDelTime, 0L));
        if (existing == null) {
            throw new ClientException(LinkErrorCodeEnum.LINK_NOT_EXIST);
        }

        // 构建新的短链对象
        LinkDO linkDO = LinkDO.builder()
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .validDateType(requestParam.getValidDateType())
                .validDate(Objects.equals(requestParam.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()) ? null : requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .domain(existing.getDomain())
                .shortUri(existing.getShortUri())
                .clickNum(existing.getClickNum())
                .favicon(existing.getFavicon())
                .createdType(existing.getCreatedType())
                .enableStatus(existing.getEnableStatus())
                .delTime(0L)
                .build();

        // 如果原始短链和新的短链属于同一分组，直接更新；否则删除原始短链并新增新的短链
        if (Objects.equals(requestParam.getOriginGid(), requestParam.getGid())) {
            update(
                    linkDO, Wrappers.lambdaUpdate(LinkDO.class)
                            .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                            .eq(LinkDO::getGid, requestParam.getOriginGid())
                            .eq(LinkDO::getDelFlag, 0)
                            .eq(LinkDO::getDelTime, 0L)
                            .set(Objects.equals(requestParam.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()), LinkDO::getValidDate, null)
            );
        } else {
            remove(Wrappers.lambdaQuery(LinkDO.class)
                    .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(LinkDO::getGid, requestParam.getOriginGid())
                    .eq(LinkDO::getDelFlag, 0)
                    .eq(LinkDO::getDelTime, 0L));
            save(linkDO);
        }
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

    @Override
    public List<LinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        return linkMapper.listGroupShortLinkCount(requestParam);
    }

    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response) {
        String serverName = request.getServerName();
        String serverPort = Optional.of(request.getServerPort())
                .filter(each -> !Objects.equals(each, 80))
                .map(String::valueOf)
                .map(each -> ":" + each)
                .orElse("");
        String fullShortUrl = serverName + serverPort + "/" + shortUri;
        String gotoShortLinkKey = String.format(GOTO_SHORT_LINK_KEY, fullShortUrl);
        String gotoIsNullShortLinkKey = String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl);

        // 1. 正向缓存命中直接跳转，热点链接走这里
        String originalLink = stringRedisTemplate.opsForValue().get(gotoShortLinkKey);
        if (StringUtils.isNotBlank(originalLink)) {
            linkStatsService.saveStats(fullShortUrl, null, request, response);
            response.sendRedirect(originalLink);
            return;
        }

        // 2. BF 拦截不存在的短链，防缓存穿透
        if (!shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl)) {
            response.sendRedirect("/page/notfound");
            return;
        }

        // 3. 空值缓存拦截已删除/过期的短链（BF 无法感知删除）
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(gotoIsNullShortLinkKey);
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
            response.sendRedirect("/page/notfound");
            return;
        }

        // 4. 缓存未命中，加锁回源，防缓存击穿
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            // 双重检查，避免多个线程重复回源
            originalLink = stringRedisTemplate.opsForValue().get(gotoShortLinkKey);
            if (StringUtils.isNotBlank(originalLink)) {
                linkStatsService.saveStats(fullShortUrl, null, request, response);
                response.sendRedirect(originalLink);
                return;
            }
            gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(gotoIsNullShortLinkKey);
            if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
                response.sendRedirect("/page/notfound");
                return;
            }

            // 查路由表获取 gid（t_link 按 gid 分片，查询必须带 gid）
            LambdaQueryWrapper<LinkGotoDO> queryWrapper = Wrappers.lambdaQuery(LinkGotoDO.class)
                    .eq(LinkGotoDO::getFullShortUrl, fullShortUrl);
            LinkGotoDO linkGotoDO = linkGotoMapper.selectOne(queryWrapper);
            if (linkGotoDO == null) {
                stringRedisTemplate.opsForValue().set(gotoIsNullShortLinkKey, "-", GOTO_IS_NULL_SHORT_LINK_EXPIRE_SECONDS, TimeUnit.SECONDS);
                response.sendRedirect("/page/notfound");
                return;
            }

            LinkDO linkDO = getOne(Wrappers.lambdaQuery(LinkDO.class)
                    .eq(LinkDO::getGid, linkGotoDO.getGid())
                    .eq(LinkDO::getFullShortUrl, fullShortUrl)
                    .eq(LinkDO::getEnableStatus, 0)
                    .eq(LinkDO::getDelFlag, 0)
                    .eq(LinkDO::getDelTime, 0L));
            // 短链不存在或已过期，写空值缓存避免后续重复回源
            if (linkDO == null || (linkDO.getValidDate() != null && linkDO.getValidDate().before(new Date()))) {
                stringRedisTemplate.opsForValue().set(gotoIsNullShortLinkKey, "-", GOTO_IS_NULL_SHORT_LINK_EXPIRE_SECONDS, TimeUnit.SECONDS);
                response.sendRedirect("/page/notfound");
                return;
            }

            stringRedisTemplate.opsForValue().set(
                    gotoShortLinkKey,
                    linkDO.getOriginUrl(),
                    LinkUtil.getLinkCacheValidTime(linkDO.getValidDate()), TimeUnit.MILLISECONDS
            );
            linkStatsService.saveStats(fullShortUrl, linkGotoDO.getGid(), request, response);
            response.sendRedirect(linkDO.getOriginUrl());
        } finally {
            lock.unlock();
        }


    }

    /**
     * 短链接 URI 生成
     *
     * @param originUrl 原始链接
     * @return 短链接 URI
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

    /**
     * 获取图标
     *
     * @param originUrl 原始链接
     * @return 图标链接
     */
    private String getFavicon(String originUrl) {
        try {
            URL targetUrl = new URL(originUrl);
            HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.connect();
            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                Document document = Jsoup.connect(originUrl).timeout(3000).get();
                Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
                if (faviconLink != null) {
                    return faviconLink.attr("abs:href");
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
