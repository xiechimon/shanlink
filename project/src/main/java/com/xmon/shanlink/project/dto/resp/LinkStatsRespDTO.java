package com.xmon.shanlink.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 短链接监控响应实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkStatsRespDTO {

    /**
     * 历史PV
     */
    private Integer pv;

    /**
     * 历史UV
     */
    private Integer uv;

    /**
     * 历史UIP
     */
    private Integer uip;

    /**
     * 今日PV
     */
    private Integer todayPv;

    /**
     * 今日UV
     */
    private Integer todayUv;

    /**
     * 今日UIP
     */
    private Integer todayUip;

    /**
     * 按日期分组的访问量
     */
    private List<LinkStatsAccessDailyRespDTO> daily;

    /**
     * 地区统计
     */
    private List<LinkStatsLocaleCNRespDTO> localeCnStats;

    /**
     * 小时访问统计（24小时）
     */
    private List<Integer> hourStats;

    /**
     * 浏览器统计
     */
    private List<LinkStatsBrowserRespDTO> browserStats;

    /**
     * 操作系统统计
     */
    private List<LinkStatsOsRespDTO> osStats;

    /**
     * 星期访问统计（7天）
     */
    private List<Integer> weekdayStats;

    /**
     * 设备统计
     */
    private List<LinkStatsDeviceRespDTO> deviceStats;

    /**
     * 网络统计
     */
    private List<LinkStatsNetworkRespDTO> networkStats;

    /**
     * 高频访问IP
     */
    private List<LinkStatsTopIpRespDTO> topIpStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkStatsAccessDailyRespDTO {
        /**
         * 日期
         */
        private String date;
        /**
         * 访问量
         */
        private Integer pv;
        /**
         * 独立访客数
         */
        private Integer uv;
        /**
         * 独立IP数
         */
        private Integer uip;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkStatsLocaleCNRespDTO {
        /**
         * 访问量
         */
        private Integer cnt;
        /**
         * 省份
         */
        private String locale;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkStatsBrowserRespDTO {
        /**
         * 浏览器访问量
         */
        private Integer cnt;
        /**
         * 浏览器
         */
        private String browser;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkStatsOsRespDTO {
        /**
         * 操作系统访问量
         */
        private Integer cnt;
        /**
         * 操作系统
         */
        private String os;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkStatsDeviceRespDTO {
        /**
         * 设备访问量
         */
        private Integer cnt;
        /**
         * 设备
         */
        private String device;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkStatsNetworkRespDTO {
        /**
         * 网络访问量
         */
        private Integer cnt;
        /**
         * 网络类型
         */
        private String network;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkStatsTopIpRespDTO {
        /**
         * 统计
         */
        private Integer cnt;
        /**
         * IP
         */
        private String ip;
    }
}
