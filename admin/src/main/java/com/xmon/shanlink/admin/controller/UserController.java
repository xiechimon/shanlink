package com.xmon.shanlink.admin.controller;

import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制层
 */
@RestController
@RequestMapping("/api/shan-link/admin/v1")
public class UserController {

    /**
     * 根据用户名查询用户信息
     */
    @GetMapping("/user/{username}")
    public String getUserByUsername(@PathVariable String username) {
        return "Hi! " + username;
    }

}
