package com.minimall.module.user.service;

import cn.dev33.satoken.stp.StpLogic;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minimall.common.exception.BusinessException;
import com.minimall.common.result.ResultCode;
import com.minimall.module.user.mapper.UserMapper;
import com.minimall.module.user.model.dto.LoginDTO;
import com.minimall.module.user.model.dto.RegisterDTO;
import com.minimall.module.user.model.entity.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * C 端用户服务：注册 / 登录 / 注销
 * 注意：必须用 @Qualifier("stpUser") 显式注入 C 端登录体系，
 * 否则会命中 @Primary 导致与后台 admin 体系串号（安全隐患）。
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    private final StpLogic stpUser;

    public UserService(@Qualifier("stpUser") StpLogic stpUser) {
        this.stpUser = stpUser;
    }

    /** 注册（手机号 + 密码） */
    public Long register(RegisterDTO dto) {
        Long exist = this.lambdaQuery().eq(User::getMobile, dto.getMobile()).count();
        if (exist != null && exist > 0) {
            throw new BusinessException(ResultCode.MOBILE_ALREADY_BOUND);
        }
        User user = new User();
        user.setSn(IdUtil.getSnowflakeNextIdStr());
        user.setMobile(dto.getMobile());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setNickname("用户_" + dto.getMobile().substring(7));
        user.setSex(0);
        user.setBalance(0L);
        user.setIntegral(0L);
        user.setTotalConsume(0L);
        user.setTotalOrderNum(0);
        user.setStatus(1);
        user.setChannel(dto.getChannel());
        this.save(user);
        return user.getId();
    }

    /** 登录：返回 token，并写入会话 */
    public String login(LoginDTO dto) {
        User user = this.lambdaQuery()
                .eq(User::getMobile, dto.getMobile())
                .one();
        if (user == null || !BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        this.lambdaUpdate().eq(User::getId, user.getId())
                .set(User::getLastLoginTime, LocalDateTime.now())
                .update();
        stpUser.login(user.getId());
        return stpUser.getTokenValue();
    }

    public void logout() {
        stpUser.logout();
    }

    public Long getCurrentUserId() {
        return stpUser.getLoginIdAsLong();
    }

    public User getCurrentUser() {
        return this.getById(getCurrentUserId());
    }
}
