package com.minimall.module.distribution.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.distribution.model.entity.DistributionOrderGoods;
import com.minimall.module.distribution.service.DistributionService;
import com.minimall.module.user.model.entity.User;
import com.minimall.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * C 端分销
 */
@Tag(name = "C端-分销")
@RestController
@RequestMapping("/shop/distribution")
@RequiredArgsConstructor
@SaCheckLogin(type = "user")
public class DistributionController {

    private final DistributionService distributionService;
    private final UserService userService;

    @Operation(summary = "绑定推荐人")
    @PostMapping("/bind")
    public R<Void> bind(@RequestParam Long referrerId) {
        distributionService.bind(userService.getCurrentUserId(), referrerId);
        return R.ok("绑定成功", null);
    }

    @Operation(summary = "用邀请码绑定推荐人")
    @PostMapping("/bind-code")
    public R<Void> bindByCode(@RequestParam String code) {
        distributionService.bindByCode(userService.getCurrentUserId(), code);
        return R.ok("绑定成功", null);
    }

    @Operation(summary = "我的推荐人")
    @GetMapping("/referrer")
    public R<User> referrer() {
        return R.ok(distributionService.referrer(userService.getCurrentUserId()));
    }

    @Operation(summary = "分销中心概览（佣金/团队/邀请码/等级进度）")
    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        return R.ok(distributionService.overview(userService.getCurrentUserId()));
    }

    @Operation(summary = "我的邀请码与推广链接")
    @GetMapping("/invite")
    public R<Map<String, Object>> invite() {
        Long userId = userService.getCurrentUserId();
        String code = distributionService.inviteCode(userId);
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put("inviteCode", code);
        m.put("inviteLink", "/#/pages/user/login?ref=" + code);
        return R.ok(m);
    }

    @Operation(summary = "我的团队（下级成员与贡献佣金）")
    @GetMapping("/team")
    public R<List<Map<String, Object>>> team() {
        return R.ok(distributionService.team(userService.getCurrentUserId()));
    }

    @Operation(summary = "我的分销等级")
    @GetMapping("/level")
    public R<Map<String, Object>> myLevel() {
        return R.ok(distributionService.myLevel(userService.getCurrentUserId()));
    }

    @Operation(summary = "佣金明细")
    @GetMapping("/commissions")
    public R<List<DistributionOrderGoods>> commissions() {
        return R.ok(distributionService.listSettled(userService.getCurrentUserId()));
    }
}
