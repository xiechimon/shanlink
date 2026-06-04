package com.xmon.shanlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xmon.shanlink.admin.dao.entity.UserDO;
import com.xmon.shanlink.admin.dto.req.UserRegisterReqDTO;
import com.xmon.shanlink.admin.dto.req.UserUpdateReqDTO;
import com.xmon.shanlink.admin.dto.resp.UserRespDTO;

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
}
