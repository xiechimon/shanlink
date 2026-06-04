package com.xmon.shanlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.xmon.shanlink.admin.common.convention.result.Result;
import com.xmon.shanlink.admin.common.convention.result.Results;
import com.xmon.shanlink.admin.dto.resp.UserActualRespDTO;
import com.xmon.shanlink.admin.dto.resp.UserRespDTO;
import com.xmon.shanlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shan-link/admin/v1")
public class UserController {

    private final UserService userService;

    /**
     * 根据用户名查询脱敏用户信息
     */
    @GetMapping("/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable String username) {
        return Results.success(userService.getUserByUsername(username));
    }

    /**
     * 根据用户名查询真实用户信息
     */
    @GetMapping("/actual/user/{username}")
    public Result<UserActualRespDTO> getActualUserByUsername(@PathVariable String username) {
        return Results.success(
                BeanUtil.toBean(userService.getUserByUsername(username), UserActualRespDTO.class)
        );
    }

}
