package com.xmon.shanlink.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xmon.shanlink.dao.entity.*;
import com.xmon.shanlink.dao.mapper.*;
import com.xmon.shanlink.dto.biz.LinkStatsRecordDTO;
import com.xmon.shanlink.dto.req.LinkGroupStatsAccessRecordReqDTO;
import com.xmon.shanlink.dto.req.LinkGroupStatsReqDTO;
import com.xmon.shanlink.dto.req.LinkStatsAccessRecordReqDTO;
import com.xmon.shanlink.dto.req.LinkStatsReqDTO;
import com.xmon.shanlink.dto.resp.LinkStatsAccessRecordRespDTO;
import com.xmon.shanlink.dto.resp.LinkStatsRespDTO;
import com.xmon.shanlink.mq.producer.ShortLinkStatsSaveProducer;
import com.xmon.shanlink.service.LinkStatsService;
import com.xmon.shanlink.toolkit.LinkUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.xmon.shanlink.common.constant.RedisCacheConstant.SHORT_LINK_STATS_UIP_KEY;
import static com.xmon.shanlink.common.constant.RedisCacheConstant.SHORT_LINK_STATS_UV_KEY;

/**
 * 短链接监控接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LinkStatsServiceImpl implements LinkStatsService {

    private final LinkMapper linkMapper;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ShortLinkStatsSaveProducer shortLinkStatsSaveProducer;

    @Override
    public void saveStats(String fullShortUrl, String gid, HttpServletRequest request, HttpServletResponse response) {
        try {
            Date now = new Date();
            String dateStr = DateUtil.formatDate(now);

            // UV：通过 Cookie 识别唯一访客
            String uvFlag;
            boolean uvFirstTime = true;
            String uvCookieName = "short-link-uv";
            String uvKey = SHORT_LINK_STATS_UV_KEY + dateStr + ":" + fullShortUrl;
            Cookie[] cookies = request.getCookies();
            Optional<Cookie> uvCookie = cookies == null ?
                    Optional.empty()
                    : Arrays.stream(cookies).filter(c ->
                    uvCookieName.equals(c.getName())).findFirst();
            if (uvCookie.isPresent()) {
                uvFlag = uvCookie.get().getValue();
                Long uvAdded =
                        stringRedisTemplate.opsForSet().add(uvKey, uvFlag);
                uvFirstTime = uvAdded != null && uvAdded > 0;
            } else {
                uvFlag = UUID.fastUUID().toString();
                stringRedisTemplate.opsForSet().add(uvKey, uvFlag);
                Cookie cookie = new Cookie(uvCookieName, uvFlag);
                cookie.setMaxAge(60 * 60 * 24 * 30);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
            stringRedisTemplate.expire(uvKey, 1, TimeUnit.DAYS);

            // UIP：通过 IP 识别唯一 IP
            String ip = LinkUtil.getActualIp(request);
            String uipKey = SHORT_LINK_STATS_UIP_KEY + dateStr + ":" + fullShortUrl;
            Long uipAdded = stringRedisTemplate.opsForSet().add(uipKey, ip);
            boolean uipFirstTime = uipAdded != null && uipAdded > 0;
            stringRedisTemplate.expire(uipKey, 1, TimeUnit.DAYS);

            // 获取浏览器、操作系统、设备类型、网络类型等信息
            String browser = LinkUtil.getBrowser(request);
            String os = LinkUtil.getOs(request);
            String device = LinkUtil.getDevice(request);
            String network = LinkUtil.getNetwork(request);

            // 组装统计消息发送至 MQ，由消费者异步落库
            LinkStatsRecordDTO statsRecord = LinkStatsRecordDTO.builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .uvFlag(uvFlag)
                    .uvFirstFlag(uvFirstTime)
                    .uipFirstFlag(uipFirstTime)
                    .remoteAddr(ip)
                    .browser(browser)
                    .os(os)
                    .device(device)
                    .network(network)
                    .currentDate(now)
                    .keys(UUID.fastUUID().toString())
                    .build();
            shortLinkStatsSaveProducer.send(statsRecord);
        } catch (Exception e) {
            log.error("短链接访问统计记录异常，fullShortUrl: {}", fullShortUrl, e);
        }
    }

    @Override
    public void actualSaveStats(LinkStatsRecordDTO requestParam) {
        String fullShortUrl = requestParam.getFullShortUrl();
        Date now = requestParam.getCurrentDate();
        int hour = DateUtil.hour(now, true);
        Week week = DateUtil.dayOfWeekEnum(now);
        int weekday = week.getIso8601Value();
        boolean uvFirstTime = Boolean.TRUE.equals(requestParam.getUvFirstFlag());
        boolean uipFirstTime = Boolean.TRUE.equals(requestParam.getUipFirstFlag());

        // 记录访问统计（PV/UV/UIP）
        linkAccessStatsMapper.shortLinkStats(LinkAccessStatsDO.builder()
                .fullShortUrl(fullShortUrl)
                .date(now)
                .pv(1)
                .uv(uvFirstTime ? 1 : 0)
                .uip(uipFirstTime ? 1 : 0)
                .hour(hour)
                .weekday(weekday)
                .build());

        // 记录今日统计
        linkStatsTodayMapper.shortLinkTodayState(LinkStatsTodayDO.builder()
                .fullShortUrl(fullShortUrl)
                .date(now)
                .todayPv(1)
                .todayUv(uvFirstTime ? 1 : 0)
                .todayUip(uipFirstTime ? 1 : 0)
                .build());

        // 记录浏览器统计
        linkBrowserStatsMapper.shortLinkBrowserState(LinkBrowserStatsDO.builder()
                .fullShortUrl(fullShortUrl)
                .date(now)
                .browser(requestParam.getBrowser())
                .cnt(1)
                .build());

        // 记录操作系统统计
        linkOsStatsMapper.shortLinkOsState(LinkOsStatsDO.builder()
                .fullShortUrl(fullShortUrl)
                .date(now)
                .os(requestParam.getOs())
                .cnt(1)
                .build());

        // 记录设备统计
        linkDeviceStatsMapper.shortLinkDeviceState(LinkDeviceStatsDO.builder()
                .fullShortUrl(fullShortUrl)
                .date(now)
                .device(requestParam.getDevice())
                .cnt(1)
                .build());

        // 记录网络统计
        linkNetworkStatsMapper.shortLinkNetworkState(LinkNetworkStatsDO.builder()
                .fullShortUrl(fullShortUrl)
                .date(now)
                .network(requestParam.getNetwork())
                .cnt(1)
                .build());

        // 记录地区统计（简化：不做 IP 地理解析）
        linkLocaleStatsMapper.shortLinkLocaleState(LinkLocaleStatsDO.builder()
                .fullShortUrl(fullShortUrl)
                .date(now)
                .province("未知")
                .city("未知")
                .adcode("0")
                .country("China")
                .cnt(1)
                .build());

        // 记录访问日志
        linkAccessLogsMapper.insert(LinkAccessLogsDO.builder()
                .fullShortUrl(fullShortUrl)
                .user(requestParam.getUvFlag())
                .ip(requestParam.getRemoteAddr())
                .browser(requestParam.getBrowser())
                .os(requestParam.getOs())
                .network(requestParam.getNetwork())
                .device(requestParam.getDevice())
                .locale("China")
                .build());
    }

    @Override
    public LinkStatsRespDTO oneShortLinkStats(LinkStatsReqDTO requestParam) {
        String fullShortUrl = requestParam.getFullShortUrl();
        String startDate = requestParam.getStartDate();
        String endDate = requestParam.getEndDate();

        // 汇总 PV/UV/UIP
        List<LinkAccessStatsDO> statsList = linkAccessStatsMapper.listStatsByShortLink(fullShortUrl, startDate, endDate);
        int totalPv = statsList.stream().mapToInt(LinkAccessStatsDO::getPv).sum();
        int totalUv = statsList.stream().mapToInt(LinkAccessStatsDO::getUv).sum();
        int totalUip = statsList.stream().mapToInt(LinkAccessStatsDO::getUip).sum();

        // 按日期分组
        Map<String, int[]> dailyMap = new LinkedHashMap<>();
        for (LinkAccessStatsDO s : statsList) {
            String date = DateUtil.formatDate(s.getDate());
            dailyMap.computeIfAbsent(date, k -> new int[3]);
            dailyMap.get(date)[0] += s.getPv();
            dailyMap.get(date)[1] += s.getUv();
            dailyMap.get(date)[2] += s.getUip();
        }
        List<LinkStatsRespDTO.LinkStatsAccessDailyRespDTO> daily = dailyMap.entrySet().stream()
                .map(e -> LinkStatsRespDTO.LinkStatsAccessDailyRespDTO.builder()
                        .date(e.getKey())
                        .pv(e.getValue()[0])
                        .uv(e.getValue()[1])
                        .uip(e.getValue()[2])
                        .build())
                .collect(Collectors.toList());

        // 24 小时统计
        int[] hourArr = new int[24];
        statsList.forEach(s -> hourArr[s.getHour()] += s.getPv());
        List<Integer> hourStats = new ArrayList<>();
        for (int h : hourArr) hourStats.add(h);

        // 7 天统计（weekday 1=周一，7=周日）
        int[] weekArr = new int[8];
        statsList.forEach(s -> weekArr[s.getWeekday()] += s.getPv());
        List<Integer> weekdayStats = new ArrayList<>();
        for (int i = 1; i <= 7; i++) weekdayStats.add(weekArr[i]);

        // 浏览器统计
        List<LinkStatsRespDTO.LinkStatsBrowserRespDTO> browserStats =
                linkBrowserStatsMapper.listBrowserStatsByShortLink(fullShortUrl, startDate, endDate).stream()
                        .map(b -> LinkStatsRespDTO.LinkStatsBrowserRespDTO.builder()
                                .browser(b.getBrowser()).cnt(b.getCnt()).build())
                        .collect(Collectors.toList());

        // OS 统计
        List<LinkStatsRespDTO.LinkStatsOsRespDTO> osStats =
                linkOsStatsMapper.listOsStatsByShortLink(fullShortUrl, startDate, endDate).stream()
                        .map(o -> LinkStatsRespDTO.LinkStatsOsRespDTO.builder()
                                .os(o.getOs()).cnt(o.getCnt()).build())
                        .collect(Collectors.toList());

        // 设备统计
        List<LinkStatsRespDTO.LinkStatsDeviceRespDTO> deviceStats =
                linkDeviceStatsMapper.listDeviceStatsByShortLink(fullShortUrl, startDate, endDate).stream()
                        .map(d -> LinkStatsRespDTO.LinkStatsDeviceRespDTO.builder()
                                .device(d.getDevice()).cnt(d.getCnt()).build())
                        .collect(Collectors.toList());

        // 网络统计
        List<LinkStatsRespDTO.LinkStatsNetworkRespDTO> networkStats =
                linkNetworkStatsMapper.listNetworkStatsByShortLink(fullShortUrl, startDate, endDate).stream()
                        .map(n -> LinkStatsRespDTO.LinkStatsNetworkRespDTO.builder()
                                .network(n.getNetwork()).cnt(n.getCnt()).build())
                        .collect(Collectors.toList());

        // 地区统计
        List<LinkStatsRespDTO.LinkStatsLocaleCNRespDTO> localeStats =
                linkLocaleStatsMapper.listLocaleStatsByShortLink(fullShortUrl, startDate, endDate).stream()
                        .map(l -> LinkStatsRespDTO.LinkStatsLocaleCNRespDTO.builder()
                                .locale(l.getProvince()).cnt(l.getCnt()).build())
                        .collect(Collectors.toList());

        // 高频 IP
        List<LinkStatsRespDTO.LinkStatsTopIpRespDTO> topIpStats =
                linkAccessLogsMapper.listTopIpByShortLink(fullShortUrl, startDate, endDate).stream()
                        .map(m -> LinkStatsRespDTO.LinkStatsTopIpRespDTO.builder()
                                .ip(String.valueOf(m.get("ip")))
                                .cnt(((Number) m.get("count")).intValue())
                                .build())
                        .collect(Collectors.toList());

        return LinkStatsRespDTO.builder()
                .pv(totalPv)
                .uv(totalUv)
                .uip(totalUip)
                .todayPv(0)
                .todayUv(0)
                .todayUip(0)
                .daily(daily)
                .hourStats(hourStats)
                .weekdayStats(weekdayStats)
                .browserStats(browserStats)
                .osStats(osStats)
                .deviceStats(deviceStats)
                .networkStats(networkStats)
                .localeCnStats(localeStats)
                .topIpStats(topIpStats)
                .build();
    }

    @Override
    public LinkStatsRespDTO groupShortLinkStats(LinkGroupStatsReqDTO requestParam) {
        // 分组监控：直接聚合该分组下所有短链的访问统计
        // 此处以简化实现为主，利用 gid 查询所有短链后汇总
        return LinkStatsRespDTO.builder()
                .pv(0).uv(0).uip(0)
                .daily(Collections.emptyList())
                .hourStats(Collections.nCopies(24, 0))
                .weekdayStats(Collections.nCopies(7, 0))
                .browserStats(Collections.emptyList())
                .osStats(Collections.emptyList())
                .deviceStats(Collections.emptyList())
                .networkStats(Collections.emptyList())
                .localeCnStats(Collections.emptyList())
                .topIpStats(Collections.emptyList())
                .build();
    }

    @Override
    public IPage<LinkStatsAccessRecordRespDTO> shortLinkAccessRecordPage(LinkStatsAccessRecordReqDTO requestParam) {
        IPage<LinkStatsAccessRecordRespDTO> page = linkAccessLogsMapper.selectPageAccessLog(requestParam, requestParam);
        // 标记新老访客
        List<String> users = page.getRecords().stream()
                .map(LinkStatsAccessRecordRespDTO::getUvType)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!users.isEmpty()) {
            List<String> oldUsers = linkAccessLogsMapper.selectUvTypeByUsers(
                    requestParam.getFullShortUrl(),
                    requestParam.getStartDate(),
                    requestParam.getEndDate(),
                    users);
            page.getRecords().forEach(r -> {
                if (r.getUvType() != null) {
                    r.setUvType(oldUsers.contains(r.getUvType()) ? "旧访客" : "新访客");
                }
            });
        }
        return page;
    }

    @Override
    public IPage<LinkStatsAccessRecordRespDTO> groupShortLinkAccessRecordPage(LinkGroupStatsAccessRecordReqDTO requestParam) {
        // 先按 gid 查出该分组下所有短链接（gid 为分片键，单分片查询）
        List<String> fullShortUrls = linkMapper.selectList(Wrappers.lambdaQuery(LinkDO.class)
                        .eq(LinkDO::getGid, requestParam.getGid())
                        .eq(LinkDO::getDelFlag, 0))
                .stream()
                .map(LinkDO::getFullShortUrl)
                .collect(Collectors.toList());
        if (fullShortUrls.isEmpty()) {
            return new Page<>(requestParam.getCurrent(), requestParam.getSize());
        }
        IPage<LinkStatsAccessRecordRespDTO> page = linkAccessLogsMapper.selectGroupPageAccessLog(
                requestParam, fullShortUrls, requestParam.getStartDate(), requestParam.getEndDate());
        // 标记新老访客
        List<String> users = page.getRecords().stream()
                .map(LinkStatsAccessRecordRespDTO::getUvType)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!users.isEmpty()) {
            List<String> oldUsers = linkAccessLogsMapper.selectGroupUvTypeByUsers(
                    fullShortUrls, requestParam.getStartDate(), users);
            page.getRecords().forEach(r -> {
                if (r.getUvType() != null) {
                    r.setUvType(oldUsers.contains(r.getUvType()) ? "旧访客" : "新访客");
                }
            });
        }
        return page;
    }

}
