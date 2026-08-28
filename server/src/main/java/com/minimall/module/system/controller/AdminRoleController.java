package com.minimall.module.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.minimall.common.result.R;
import com.minimall.module.system.model.entity.Menu;
import com.minimall.module.system.model.entity.SystemRole;
import com.minimall.module.system.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 后台角色/权限管理
 */
@Tag(name = "后台-角色权限")
@RestController
@RequestMapping("/admin/role")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminRoleController {

    private final RoleService roleService;

    @Data
    public static class RoleSave {
        private SystemRole role;
        private List<Long> menuIds;
    }

    @Operation(summary = "角色列表(含权限)")
    @GetMapping
    public R<List<Object>> roles() {
        return R.ok(roleService.roles().stream().map(r -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", r.getId());
            m.put("name", r.getName());
            m.put("code", r.getCode());
            m.put("remark", r.getRemark());
            m.put("status", r.getStatus());
            m.put("createTime", r.getCreateTime());
            m.put("menuIds", roleService.roleMenus(r.getId()).stream()
                    .map(rm -> rm.getMenuId()).collect(Collectors.toList()));
            return m;
        }).collect(Collectors.toList()));
    }

    @Operation(summary = "菜单树")
    @GetMapping("/menus")
    public R<List<Menu>> menus() {
        return R.ok(roleService.menus());
    }

    @Operation(summary = "保存角色(含权限分配)")
    @SaCheckPermission(value = "system:role", type = "admin")
    @PostMapping
    public R<Long> save(@RequestBody RoleSave body) {
        return R.ok("保存成功", roleService.saveRole(body.getRole(), body.getMenuIds()));
    }
}