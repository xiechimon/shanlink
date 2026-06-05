package com.xmon.shanlink.admin.controller;

import com.xmon.shanlink.admin.common.convention.result.Result;
import com.xmon.shanlink.admin.common.convention.result.Results;
import com.xmon.shanlink.admin.dao.entity.GroupDO;
import com.xmon.shanlink.admin.dto.req.GroupSaveReqDTO;
import com.xmon.shanlink.admin.dto.req.GroupUpdateReqDO;
import com.xmon.shanlink.admin.dto.resp.GroupRespDTO;
import com.xmon.shanlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shan-link/admin/v1/group")
public class GroupController {

    private final GroupService groupService;

    /**
     * 新增短链接分组
     */
    @PostMapping
    public Result<Void> saveGroup(@RequestBody GroupSaveReqDTO requestParam) {
        groupService.saveGroup(requestParam.getName());
        return Results.success();
    }

    /**
     * 查询用户分组集合
     */
    @GetMapping
    public Result<List<GroupRespDTO>> listGroup() {
        return Results.success(groupService.listGroup());
    }

    /**
     * 修改短链接分组
     */
    @PutMapping
    public Result<Void> updateGroup(@RequestBody GroupUpdateReqDO requestParam) {
        groupService.updateGroup(requestParam);
        return Results.success();
    }

    /**
     * 删除短链接分组
     */
    @DeleteMapping
    public Result<Void> deleteGroup(@RequestBody String gid) {
        groupService.deleteGroup(gid);
        return Results.success();
    }
}
