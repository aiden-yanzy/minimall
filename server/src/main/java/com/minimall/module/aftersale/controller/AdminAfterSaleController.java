package com.minimall.module.aftersale.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.aftersale.model.entity.AfterSale;
import com.minimall.module.aftersale.service.AfterSaleService;
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
 * 后台售后管理
 */
@Tag(name = "后台-售后管理")
@RestController
@RequestMapping("/admin/after")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminAfterSaleController {

    private final AfterSaleService afterSaleService;

    @Operation(summary = "售后列表")
    @GetMapping
    public R<List<AfterSale>> list(@RequestParam(required = false) Integer status) {
        return R.ok(afterSaleService.adminList(status));
    }

    @Operation(summary = "同意退款")
    @PostMapping("/{id}/approve")
    public R<Void> approve(@PathVariable Long id) {
        afterSaleService.approve(id);
        return R.ok("退款成功", null);
    }

    @Operation(summary = "拒绝售后")
    @PostMapping("/{id}/reject")
    public R<Void> reject(@PathVariable Long id, @RequestParam(required = false) String reason) {
        afterSaleService.reject(id, reason);
        return R.ok("已拒绝", null);
    }
}
