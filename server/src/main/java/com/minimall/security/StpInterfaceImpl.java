package com.minimall.security;

import cn.dev33.satoken.stp.StpInterface;
import com.minimall.module.system.model.entity.Admin;
import com.minimall.module.system.service.AdminService;
import com.minimall.module.user.model.entity.User;
import com.minimall.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限数据源：根据登录类型返回角色/权限。
 * - loginType = "user"  -> C 端用户（普通角色 member）
 * - loginType = "admin" -> 后台管理员（读取 admin.role）
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserService userService;
    private final AdminService adminService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 权限后端基于菜单/角色做，P0 阶段返回空，后续对接 admin 身份权限表
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        if ("admin".equals(loginType)) {
            Admin admin = adminService.getById((Long) loginId);
            if (admin != null && admin.getRoleName() != null) {
                return List.of(admin.getRoleName());
            }
            return Collections.emptyList();
        }
        if ("user".equals(loginType)) {
            User user = userService.getById((Long) loginId);
            return user != null ? List.of("member") : Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
