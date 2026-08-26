package com.minimall.module.wallet.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.wallet.model.entity.WithdrawApply;
import com.minimall.module.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台钱包管理（提现审核）
 */
@Tag(name = "后台-钱包管理")
@RestController
@RequestMapping("/admin/wallet")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminWalletController {

    private final WalletService walletService;

    @Operation(summary = "提现申请列表")
    @GetMapping("/withdraws")
    public R<List<WithdrawApply>> withdraws(@RequestParam(required = false) Integer status) {
        return R.ok(walletService.adminWithdraws(status));
    }

    @Operation(summary = "同意提现(模拟打款)")
    @PostMapping("/withdraws/{id}/approve")
    public R<Void> approve(@PathVariable Long id) {
        walletService.auditWithdraw(id, true, "审核通过");
        return R.ok("已打款", null);
    }

    @Operation(summary = "拒绝提现(退回余额)")
    @PostMapping("/withdraws/{id}/reject")
    public R<Void> reject(@PathVariable Long id, @RequestParam(required = false) String reason) {
        walletService.auditWithdraw(id, false, reason);
        return R.ok("已拒绝", null);
    }
}