package com.minimall.security;

import cn.dev33.satoken.stp.StpInterface;
import com.minimall.module.system.model.entity.Admin;
import com.minimall.module.system.service.AdminService;
import com.minimall.module.system.service.RoleService;
import com.minimall.module.user.model.entity.User;
import com.minimall.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限数据源：根据登录类型返回角色/权限。
 * - "user"  -> C 端用户（member）
 * - "admin" -> 后台管理员（按 admin.role_id 查 role_menu 的权限码，供 @SaCheckPermission 鉴权）
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserService userService;
    private final AdminService adminService;
    private final RoleService roleService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        if (loginId == null) return Collections.emptyList();
        Long lid = Long.parseLong(String.valueOf(loginId));
        if ("admin".equals(loginType)) {
            Admin admin = adminService.getById(lid);
            if (admin == null) return Collections.emptyList();
            // code=admin 的角色视为超管：返回通配，放行一切
            if (admin.getRoleId() != null && "admin".equals(
                    roleService.roles().stream().filter(r -> r.getId().equals(admin.getRoleId()))
                            .map(r -> r.getCode()).findFirst().orElse(""))) {
                return List.of("*");
            }
            return roleService.permissionCodes(admin.getRoleId());
        }
        if ("user".equals(loginType)) {
            User user = userService.getById(lid);
            return user != null ? List.of("member") : Collections.emptyList();
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        if (loginId == null) return Collections.emptyList();
        Long lid = Long.parseLong(String.valueOf(loginId));
        if ("admin".equals(loginType)) {
            Admin admin = adminService.getById(lid);
            if (admin != null && admin.getRoleName() != null) {
                return List.of(admin.getRoleName());
            }
            return Collections.emptyList();
        }
        if ("user".equals(loginType)) {
            User user = userService.getById(lid);
            return user != null ? List.of("member") : Collections.emptyList();
        }
        return Collections.emptyList();
    }
}