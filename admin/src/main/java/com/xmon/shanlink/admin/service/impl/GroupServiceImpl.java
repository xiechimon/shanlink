package com.xmon.shanlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmon.shanlink.admin.common.biz.user.UserContext;
import com.xmon.shanlink.admin.common.convention.exception.ClientException;
import com.xmon.shanlink.admin.common.enums.GroupErrorCodeEnum;
import com.xmon.shanlink.admin.dao.entity.GroupDO;
import com.xmon.shanlink.admin.dao.mapper.GroupMapper;
import com.xmon.shanlink.admin.dto.req.GroupSortReqDTO;
import com.xmon.shanlink.admin.dto.req.GroupUpdateReqDO;
import com.xmon.shanlink.admin.dto.resp.GroupRespDTO;
import com.xmon.shanlink.admin.service.GroupService;
import com.xmon.shanlink.admin.toolkit.RandomGenerator;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分组接口实现层
 */
@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    private final RBloomFilter<String> gidRegisterCachePenetrationBloomFilter;

    @Value("${shan-link.group.max-num}")
    private Integer groupMaxNum;

    @Override
    public void saveGroup(String name) {
        saveGroup(name, UserContext.getUsername());
    }

    @Override
    public void saveGroup(String name, String username) {
        // TODO 分布式锁防止同一用户重复提交创建分组请求
        List<GroupDO> groupDOList = list(Wrappers.lambdaQuery(GroupDO.class)
                                                 .eq(GroupDO::getUsername, username)
                                                 .eq(GroupDO::getDelFlag, 0));

        // 校验同一用户分组数量是否超过上限
        if (groupDOList.size() >= groupMaxNum) {
            throw new ClientException(GroupErrorCodeEnum.GROUP_MAX_NUM);
        }
        // 校验同一用户分组名是否重复
        if (groupDOList.stream().anyMatch(g -> g.getName().equals(name))) {
            throw new ClientException(GroupErrorCodeEnum.GROUP_NAME_EXIST);
        }

        // 生成唯一 gid
        String gid;
        do {
            gid = RandomGenerator.generateRandom();


        } while (gidRegisterCachePenetrationBloomFilter.contains(gid));

        // 新增分组
        GroupDO groupDO = GroupDO.builder()
                .name(name)
                .gid(gid)
                .username(username)
                .sortOrder(0)
                .build();
        try {
            save(groupDO);
        } catch (DuplicateKeyException e) {
            throw new ClientException(GroupErrorCodeEnum.GROUP_NAME_EXIST);
        }
        gidRegisterCachePenetrationBloomFilter.add(gid);
    }

    @Override
    public List<GroupRespDTO> listGroup() {
        // TODO 放缓存中避免频繁访问
        // Q：放缓存中顺序如何保证？
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0)
                .orderByDesc(GroupDO::getSortOrder);
        return BeanUtil.copyToList(list(queryWrapper), GroupRespDTO.class);
    }

    @Override
    public void updateGroup(GroupUpdateReqDO requestParam) {
        // 校验
        verifyGroupBelongsToCurrentUser(requestParam.getGid());

        // 更新
        update(Wrappers.lambdaUpdate(GroupDO.class)
                       .set(GroupDO::getName, requestParam.getName())
                       .eq(GroupDO::getGid, requestParam.getGid())
                       .eq(GroupDO::getUsername, UserContext.getUsername()));
    }

    @Override
    public void sortGroup(List<GroupSortReqDTO> requestParam) {
        requestParam.forEach(item -> update(Wrappers.lambdaUpdate(GroupDO.class)
                                                    .set(GroupDO::getSortOrder, item.getSortOrder())
                                                    .eq(GroupDO::getGid, item.getGid())
                                                    .eq(GroupDO::getUsername, UserContext.getUsername())));
    }

    @Override
    public void deleteGroup(String gid) {
        // 校验
        verifyGroupBelongsToCurrentUser(gid);

        // 删除
        update(Wrappers.lambdaUpdate(GroupDO.class)
                       .set(GroupDO::getDelFlag, 1)
                       .eq(GroupDO::getGid, gid)
                       .eq(GroupDO::getUsername, UserContext.getUsername()));
    }

    @Override
    public void verifyGroupBelongsToCurrentUser(String gid) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getDelFlag, 0);
        GroupDO groupDO = getOne(queryWrapper);
        if (groupDO == null) {
            throw new ClientException(GroupErrorCodeEnum.GROUP_NOT_EXIST);
        }
    }
}
