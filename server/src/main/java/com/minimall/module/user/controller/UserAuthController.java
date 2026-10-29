package com.minimall.module.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.minimall.common.result.R;
import com.minimall.module.user.model.dto.LoginDTO;
import com.minimall.module.user.model.dto.RegisterDTO;
import com.minimall.module.user.model.entity.User;
import com.minimall.module.user.model.vo.LoginVO;
import com.minimall.module.user.model.vo.UserVO;
import com.minimall.module.lottery.service.LotteryService;
import com.minimall.module.user.service.UserService;
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
 * C 端用户认证接口
 */
@Tag(name = "C端-用户认证")
@RestController
@RequestMapping("/shop/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserService userService;
    private final LotteryService lotteryService;
    private final com.minimall.module.distribution.service.DistributionService distributionService;

    @Operation(summary = "注册")
    @SaIgnore
    @PostMapping("/register")
    public R<Long> register(@Valid @RequestBody RegisterDTO dto) {
        Long id = userService.register(dto);
        // 注册带邀请码/推荐人 → 自动绑定分销关系（失败不影响注册）
        try {
            if (dto.getInviteCode() != null && !dto.getInviteCode().isBlank()) {
                distributionService.bindByCode(id, dto.getInviteCode());
            } else if (dto.getReferrerId() != null) {
                distributionService.bind(id, dto.getReferrerId());
            }
        } catch (Exception ignore) {
        }
        // 注册送奖励（失败不影响注册）
        try {
            lotteryService.grantRegisterAward(id);
        } catch (Exception ignore) {
        }
        return R.ok("注册成功", id);
    }

    @Operation(summary = "登录")
    @SaIgnore
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        String token = userService.login(dto);
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        return R.ok("登录成功", vo);
    }

    @Operation(summary = "退出登录")
    @SaCheckLogin(type = "user")
    @PostMapping("/logout")
    public R<Void> logout() {
        userService.logout();
        return R.ok();
    }

    @Operation(summary = "当前登录用户信息")
    @SaCheckLogin(type = "user")
    @GetMapping("/me")
    public R<UserVO> me() {
        User user = userService.getCurrentUser();
        return R.ok(UserVO.from(user));
    }
}
