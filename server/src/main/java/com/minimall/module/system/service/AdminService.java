package com.minimall.module.system.service;

import cn.dev33.satoken.stp.StpLogic;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minimall.common.exception.BusinessException;
import com.minimall.common.result.ResultCode;
import com.minimall.module.system.mapper.AdminMapper;
import com.minimall.module.system.model.dto.AdminLoginDTO;
import com.minimall.module.system.model.entity.Admin;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 后台管理员服务
 * 注意：必须用 @Qualifier("stpAdmin") 注入后台登录体系，避免与 C 端 user 体系串号。
 */
@Service
public class AdminService extends ServiceImpl<AdminMapper, Admin> {

    private final StpLogic stpAdmin;

    public AdminService(@Qualifier("stpAdmin") StpLogic stpAdmin) {
        this.stpAdmin = stpAdmin;
    }

    /** 后台登录 */
    public String login(AdminLoginDTO dto) {
        Admin admin = this.lambdaQuery().eq(Admin::getAccount, dto.getAccount()).one();
        if (admin == null || !BCrypt.checkpw(dto.getPassword(), admin.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        if (admin.getStatus() == null || admin.getStatus() != 1) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        this.lambdaUpdate().eq(Admin::getId, admin.getId())
                .set(Admin::getLastLoginTime, LocalDateTime.now())
                .update();
        stpAdmin.login(admin.getId());
        return stpAdmin.getTokenValue();
    }

    public void logout() {
        stpAdmin.logout();
    }

    public Long getCurrentAdminId() {
        return stpAdmin.getLoginIdAsLong();
    }

    public Admin getCurrentAdmin() {
        return this.getById(getCurrentAdminId());
    }

    /** 当前管理员的权限码集合（走 admin 体系，别用默认 StpUtil） */
    public java.util.List<String> getCurrentPermissions() {
        return stpAdmin.getPermissionList();
    }
}
