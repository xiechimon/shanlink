package com.xmon.shanlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmon.shanlink.project.dao.entity.LinkDO;
import com.xmon.shanlink.project.dao.mapper.LinkMapper;
import com.xmon.shanlink.project.dto.req.RecycleBinPageReqDTO;
import com.xmon.shanlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.xmon.shanlink.project.dto.req.RecycleBinRemoveReqDTO;
import com.xmon.shanlink.project.dto.req.RecycleBinSaveReqDTO;
import com.xmon.shanlink.project.dto.resp.LinkPageRespDTO;
import com.xmon.shanlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import static com.xmon.shanlink.project.common.constant.RedisCacheConstant.GOTO_IS_NULL_SHORT_LINK_KEY;
import static com.xmon.shanlink.project.common.constant.RedisCacheConstant.GOTO_SHORT_LINK_KEY;

/**
 * 回收站管理接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<LinkMapper, LinkDO> implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void saveRecycleBin(RecycleBinSaveReqDTO requestParam) {
        // 将短链接状态修改为禁用
        LambdaUpdateWrapper<LinkDO> updateWrapper = Wrappers.lambdaUpdate(LinkDO.class)
                .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(LinkDO::getGid, requestParam.getGid())
                .eq(LinkDO::getEnableStatus, 0)
                .eq(LinkDO::getDelFlag, 0);
        LinkDO shortLinkDO = LinkDO.builder()
                .enableStatus(1)
                .build();
        baseMapper.update(shortLinkDO, updateWrapper);
        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
    }

    @Override
    public void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam) {
        LambdaUpdateWrapper<LinkDO> updateWrapper = Wrappers.lambdaUpdate(LinkDO.class)
                .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(LinkDO::getGid, requestParam.getGid())
                .eq(LinkDO::getEnableStatus, 1)
                .eq(LinkDO::getDelFlag, 0);
        LinkDO linkDO = LinkDO.builder()
                .enableStatus(0)
                .build();
        baseMapper.update(linkDO, updateWrapper);
        stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
    }

    @Override
    public void removeRecycleBin(RecycleBinRemoveReqDTO requestParam) {
        LambdaUpdateWrapper<LinkDO> updateWrapper = Wrappers.lambdaUpdate(LinkDO.class)
                .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(LinkDO::getGid, requestParam.getGid())
                .eq(LinkDO::getEnableStatus, 1)
                .eq(LinkDO::getDelFlag, 0)
                .set(LinkDO::getDelFlag, 1);
        baseMapper.update(null, updateWrapper);
    }

    @Override
    public IPage<LinkPageRespDTO> pageRecycleBin(RecycleBinPageReqDTO requestParam) {
        LambdaQueryWrapper<LinkDO> queryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                .in(!CollectionUtils.isEmpty(requestParam.getGidList()), LinkDO::getGid, requestParam.getGidList())
                .eq(LinkDO::getEnableStatus, 1)
                .eq(LinkDO::getDelFlag, 0)
                .orderByDesc(LinkDO::getUpdateTime);
        IPage<LinkDO> page = page(requestParam, queryWrapper);
        return page.convert(each -> BeanUtil.toBean(each, LinkPageRespDTO.class));
    }
}
