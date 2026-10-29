package com.minimall.module.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.minimall.common.result.R;
import com.minimall.module.system.model.dto.AdminLoginDTO;
import com.minimall.module.system.model.entity.Admin;
import com.minimall.module.system.model.vo.AdminVO;
import com.minimall.module.system.model.vo.LoginVO;
import com.minimall.module.system.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台管理员认证接口
 */
@Tag(name = "后台-管理员认证")
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;


    @Operation(summary = "后台登录")
    @SaIgnore
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody AdminLoginDTO dto) {
        String token = adminService.login(dto);
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        return R.ok("登录成功", vo);
    }

    @Operation(summary = "退出登录")
    @SaCheckLogin(type = "admin")
    @PostMapping("/logout")
    public R<Void> logout() {
        adminService.logout();
        return R.ok();
    }

    @Operation(summary = "当前管理员信息")
    @SaCheckLogin(type = "admin")
    @GetMapping("/me")
    public R<AdminVO> me() {
        Admin admin = adminService.getCurrentAdmin();
        AdminVO vo = AdminVO.from(admin);
        vo.setRoleCode(admin.getRoleName());
        vo.setPermissions(adminService.getCurrentPermissions());
        return R.ok(vo);
    }
}
