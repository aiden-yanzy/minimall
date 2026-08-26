package com.minimall.module.wallet.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.common.result.ResultCode;
import com.minimall.module.user.model.entity.User;
import com.minimall.module.user.service.UserService;
import com.minimall.module.wallet.model.dto.RechargeDTO;
import com.minimall.module.wallet.model.dto.WithdrawDTO;
import com.minimall.module.wallet.model.entity.AccountLog;
import com.minimall.module.wallet.model.entity.RechargeOrder;
import com.minimall.module.wallet.model.entity.RechargeTemplate;
import com.minimall.module.wallet.model.entity.WithdrawApply;
import com.minimall.module.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端钱包
 */
@Tag(name = "C端-钱包")
@RestController
@RequestMapping("/shop/wallet")
@RequiredArgsConstructor
@SaCheckLogin(type = "user")
public class WalletController {

    private final WalletService walletService;
    private final UserService userService;

    @Operation(summary = "余额")
    @GetMapping("/balance")
    public R<Long> balance() {
        User u = userService.getById(userService.getCurrentUserId());
        return R.ok(u == null || u.getBalance() == null ? 0 : u.getBalance());
    }

    @Operation(summary = "充值档位")
    @GetMapping("/templates")
    public R<List<RechargeTemplate>> templates() {
        return R.ok(walletService.templates());
    }

    @Operation(summary = "创建充值单")
    @PostMapping("/recharge")
    public R<RechargeOrder> recharge(@RequestBody RechargeDTO dto) {
        return R.ok("请支付", walletService.createRecharge(userService.getCurrentUserId(), dto.getTemplateId(), dto.getAmount()));
    }

    @Operation(summary = "充值支付(模拟)")
    @PostMapping("/recharge/pay")
    public R<Void> payRecharge(@RequestParam String orderSn) {
        walletService.payRecharge(userService.getCurrentUserId(), orderSn);
        return R.ok("充值成功", null);
    }

    @Operation(summary = "我的充值记录")
    @GetMapping("/recharges")
    public R<List<RechargeOrder>> recharges() {
        return R.ok(walletService.myRecharges(userService.getCurrentUserId()));
    }

    @Operation(summary = "账户流水")
    @GetMapping("/logs")
    public R<List<AccountLog>> logs(@RequestParam(required = false) Integer type) {
        return R.ok(walletService.logs(userService.getCurrentUserId(), type));
    }

    @Operation(summary = "申请提现")
    @PostMapping("/withdraw")
    public R<Long> withdraw(@Valid @RequestBody WithdrawDTO dto) {
        return R.ok("已提交审核", walletService.withdraw(userService.getCurrentUserId(), dto.getAmount(), dto.getType(), dto.getAccountInfo()));
    }

    @Operation(summary = "我的提现记录")
    @GetMapping("/withdraws")
    public R<List<WithdrawApply>> withdraws() {
        return R.ok(walletService.myWithdraws(userService.getCurrentUserId()));
    }
}