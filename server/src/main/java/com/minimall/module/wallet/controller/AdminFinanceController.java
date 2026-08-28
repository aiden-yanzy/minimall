package com.minimall.module.wallet.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minimall.common.result.R;
import com.minimall.module.distribution.mapper.DistributionOrderGoodsMapper;
import com.minimall.module.distribution.model.entity.DistributionOrderGoods;
import com.minimall.module.wallet.mapper.AccountLogMapper;
import com.minimall.module.wallet.model.entity.AccountLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台财务明细：账户流水 / 分销佣金
 */
@Tag(name = "后台-财务明细")
@RestController
@RequestMapping("/admin/finance")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminFinanceController {

    private final AccountLogMapper accountLogMapper;
    private final DistributionOrderGoodsMapper dogMapper;

    @Operation(summary = "账户流水")
    @GetMapping("/logs")
    public R<IPage<AccountLog>> logs(@RequestParam(defaultValue = "1") long page,
                                     @RequestParam(defaultValue = "15") long size,
                                     @RequestParam(required = false) Long userId,
                                     @RequestParam(required = false) Integer type) {
        return R.ok(accountLogMapper.selectPage(new Page<>(page, size),
                Wrappers.<AccountLog>lambdaQuery()
                        .eq(userId != null, AccountLog::getUserId, userId)
                        .eq(type != null, AccountLog::getType, type)
                        .orderByDesc(AccountLog::getId)));
    }

    @Operation(summary = "分销佣金明细")
    @GetMapping("/commissions")
    public R<IPage<DistributionOrderGoods>> commissions(@RequestParam(defaultValue = "1") long page,
                                                        @RequestParam(defaultValue = "15") long size,
                                                        @RequestParam(required = false) Long sellerUserId) {
        return R.ok(dogMapper.selectPage(new Page<>(page, size),
                Wrappers.<DistributionOrderGoods>lambdaQuery()
                        .eq(sellerUserId != null, DistributionOrderGoods::getSellerUserId, sellerUserId)
                        .orderByDesc(DistributionOrderGoods::getId)));
    }
}