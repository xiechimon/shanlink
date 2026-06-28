package com.xmon.shanlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xmon.shanlink.dao.entity.LinkAccessLogsDO;
import com.xmon.shanlink.dto.req.LinkStatsAccessRecordReqDTO;
import com.xmon.shanlink.dto.resp.LinkStatsAccessRecordRespDTO;
import com.xmon.shanlink.dto.resp.LinkStatsRespDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 短链接访问日志持久层
 */
public interface LinkAccessLogsMapper extends BaseMapper<LinkAccessLogsDO> {

    /**
     * 分页查询访问日志
     *
     * @param requestParam 请求参数
     * @return 访问日志分页数据
     */
    IPage<LinkStatsAccessRecordRespDTO> selectPageAccessLog(IPage<LinkStatsAccessRecordRespDTO> page,
                                                            @Param("param") LinkStatsAccessRecordReqDTO requestParam);

    /**
     * 分页查询分组访问日志
     *
     * @param page          分页参数
     * @param fullShortUrls 分组下短链接集合
     * @param startDate     开始日期
     * @param endDate       结束日期
     * @return 访问日志分页数据
     */
    IPage<LinkStatsAccessRecordRespDTO> selectGroupPageAccessLog(IPage<LinkStatsAccessRecordRespDTO> page,
                                                                 @Param("fullShortUrls") List<String> fullShortUrls,
                                                                 @Param("startDate") String startDate,
                                                                 @Param("endDate") String endDate);

    /**
     * 查询分组指定日期范围内新增 UV 的用户
     *
     * @param fullShortUrls 分组下短链接集合
     * @param startDate     开始日期
     * @param users         待判定用户集合
     * @return UV 用户列表
     */
    List<String> selectGroupUvTypeByUsers(@Param("fullShortUrls") List<String> fullShortUrls,
                                          @Param("startDate") String startDate,
                                          @Param("users") List<String> users);

    /**
     * 查询指定日期范围内新增 UV 的用户
     *
     * @param fullShortUrl 完整短链接
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return UV 用户列表
     */
    List<String> selectUvTypeByUsers(@Param("fullShortUrl") String fullShortUrl,
                                     @Param("startDate") String startDate,
                                     @Param("endDate") String endDate,
                                     @Param("users") List<String> users);

    /**
     * 查询指定日期范围内高频访问IP
     *
     * @param fullShortUrl 完整短链接
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 高频访问IP集合
     */
    List<Map<String, Object>> listTopIpByShortLink(@Param("fullShortUrl") String fullShortUrl,
                                                   @Param("startDate") String startDate,
                                                   @Param("endDate") String endDate);

    /**
     * 查询指定日期范围内新老访客统计
     *
     * @param fullShortUrl 完整短链接
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 新老访客统计
     */
    List<LinkStatsRespDTO.LinkStatsUvTypeRespDTO> listUvTypeStatsByShortLink(@Param("fullShortUrl") String fullShortUrl,
                                                                             @Param("startDate") String startDate,
                                                                             @Param("endDate") String endDate);
}
