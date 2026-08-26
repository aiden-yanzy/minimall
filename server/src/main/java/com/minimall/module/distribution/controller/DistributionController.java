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

import java.util.HashMap;
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

    @Operation(summary = "我的推荐人")
    @GetMapping("/referrer")
    public R<User> referrer() {
        return R.ok(distributionService.referrer(userService.getCurrentUserId()));
    }

    @Operation(summary = "我的佣金概览")
    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        Long userId = userService.getCurrentUserId();
        Map<String, Object> m = new HashMap<>();
        m.put("totalCommission", distributionService.myCommission(userId));
        m.put("balance", userService.getById(userId).getBalance());
        m.put("orders", distributionService.listSettled(userId));
        return R.ok(m);
    }

    @Operation(summary = "佣金明细")
    @GetMapping("/commissions")
    public R<List<DistributionOrderGoods>> commissions() {
        return R.ok(distributionService.listSettled(userService.getCurrentUserId()));
    }
}