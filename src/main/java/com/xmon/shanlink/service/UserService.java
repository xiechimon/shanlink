package com.xmon.shanlink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xmon.shanlink.dao.entity.UserDO;
import com.xmon.shanlink.dto.req.UserLoginReqDTO;
import com.xmon.shanlink.dto.req.UserRegisterReqDTO;
import com.xmon.shanlink.dto.req.UserUpdateReqDTO;
import com.xmon.shanlink.dto.resp.UserLoginRespDTO;
import com.xmon.shanlink.dto.resp.UserRespDTO;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户返回实体
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 查询用户名是否可用
     *
     * @param username 用户名字
     * @return true 可用，false 不可用
     */
    Boolean checkUsername(String username);

    /**
     * 注册用户
     *
     * @param requestParam 用户注册请求参数
     */
    void register(UserRegisterReqDTO requestParam);

    /**
     * 更新用户信息
     *
     * @param requestParam 用户更新请求参数
     */
    void update(UserUpdateReqDTO requestParam);

    /**
     * 用户登录
     *
     * @param requestParam 用户登录请求参数
     * @return 用户登录返回参数
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    /**
     * 校验用户登录状态
     *
     * @param username 用户名
     * @param token    登录令牌
     * @return true 已登录，false 未登录
     */
    Boolean checkLogin(String username, String token);

    /**
     * 用户推出登陆
     * @param username 用户名
     * @param token 登陆令牌
     */
    void logout(String username, String token);
}
