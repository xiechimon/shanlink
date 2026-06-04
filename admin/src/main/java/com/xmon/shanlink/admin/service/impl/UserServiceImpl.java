package com.xmon.shanlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmon.shanlink.admin.common.convention.exception.ClientException;
import com.xmon.shanlink.admin.common.enums.UserErrorCodeEnum;
import com.xmon.shanlink.admin.dao.entity.UserDO;
import com.xmon.shanlink.admin.dao.mapper.UserMapper;
import com.xmon.shanlink.admin.dto.req.UserLoginReqDTO;
import com.xmon.shanlink.admin.dto.req.UserRegisterReqDTO;
import com.xmon.shanlink.admin.dto.req.UserUpdateReqDTO;
import com.xmon.shanlink.admin.dto.resp.UserLoginRespDTO;
import com.xmon.shanlink.admin.dto.resp.UserRespDTO;
import com.xmon.shanlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.xmon.shanlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static com.xmon.shanlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;
import static com.xmon.shanlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_TTL;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = getOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }

        return BeanUtil.toBean(userDO, UserRespDTO.class);
    }

    @Override
    public Boolean checkUsername(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterReqDTO requestParam) {
        if (checkUsername(requestParam.getUsername())) {
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        }

        // 分布式锁，防止恶意请求大量使用相同用户名进行注册
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParam.getUsername());
        if (!lock.tryLock()) {
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        }
        try {
            save(BeanUtil.toBean(requestParam, UserDO.class));
            userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
        } catch (DuplicateKeyException e) {
            throw new ClientException(UserErrorCodeEnum.USER_EXIST);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void update(UserUpdateReqDTO requestParam) {
        // TODO：后续可以增加用户信息修改权限校验
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername());

        update(BeanUtil.toBean(requestParam, UserDO.class), queryWrapper);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        // 1. 查询用户
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername())
                .eq(UserDO::getDelFlag, 0);
        UserDO userDO = getOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        // 2. 验证密码
        if (!userDO.getPassword().equals(requestParam.getPassword())) {
            throw new ClientException(UserErrorCodeEnum.USER_PASSWORD_ERROR);
        }
        // 3. 已登录则续期并返回已有 token
        String loginKey = USER_LOGIN_KEY + requestParam.getUsername();

        Map<Object, Object> loginMap = stringRedisTemplate.opsForHash().entries(loginKey);
        if (CollUtil.isNotEmpty(loginMap)) {
            stringRedisTemplate.expire(loginKey, USER_LOGIN_TTL, TimeUnit.DAYS);
            String token = loginMap.keySet().iterator().next().toString();
            return new UserLoginRespDTO(token);
        }
        // 4. 生成 token，存入 Redis Hash，过期时间 30 天
        String token = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put(loginKey, token, JSON.toJSONString(userDO));
        stringRedisTemplate.expire(loginKey, USER_LOGIN_TTL, TimeUnit.DAYS);
        // 5. 返回 token
        return new UserLoginRespDTO(token);
    }
}
