package com.xmon.shanlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.xmon.shanlink.admin.common.convention.result.Result;
import com.xmon.shanlink.admin.common.convention.result.Results;
import com.xmon.shanlink.admin.dao.entity.UserDO;
import com.xmon.shanlink.admin.dto.req.UserRegisterReqDTO;
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
@RequestMapping("/api/shan-link/admin/v1/user")
public class UserController {

    private final UserService userService;

    /**
     * 根据用户名查询脱敏用户信息
     */
    @GetMapping("/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable String username) {
        return Results.success(userService.getUserByUsername(username));
    }

    /**
     * 根据用户名查询真实用户信息
     */
    @GetMapping("/actual/{username}")
    public Result<UserActualRespDTO> getActualUserByUsername(@PathVariable String username) {
        return Results.success(
                BeanUtil.toBean(userService.getUserByUsername(username), UserActualRespDTO.class)
        );
    }

    /**
     * 查询用户名是否存在
     */
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam("username") String username) {
        return Results.success(userService.checkUsername(username));
    }


    @PostMapping
    public Result<Void> register(@RequestBody UserRegisterReqDTO requestParam) {
        userService.register(requestParam);
        return Results.success();
    }
}
