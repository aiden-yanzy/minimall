package com.minimall.module.integral.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.integral.model.entity.IntegralGoods;
import com.minimall.module.integral.model.entity.IntegralOrder;
import com.minimall.module.integral.service.IntegralGoodsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台积分商城管理
 */
@Tag(name = "后台-积分商城")
@RestController
@RequestMapping("/admin/integral")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminIntegralController {

    private final IntegralGoodsService integralGoodsService;

    @Operation(summary = "积分商品列表")
    @GetMapping("/goods")
    public R<List<IntegralGoods>> goodsList() {
        return R.ok(integralGoodsService.list());
    }

    @Operation(summary = "保存积分商品")
    @PostMapping("/goods")
    public R<Long> saveGoods(@RequestBody IntegralGoods g) {
        integralGoodsService.saveOrUpdate(g);
        return R.ok("保存成功", g.getId());
    }

    @Operation(summary = "删除积分商品")
    @DeleteMapping("/goods/{id}")
    public R<Void> removeGoods(@PathVariable Long id) {
        integralGoodsService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "兑换订单列表")
    @GetMapping("/orders")
    public R<List<IntegralOrder>> orders(@RequestParam(required = false) Integer status) {
        return R.ok(integralGoodsService.adminOrders(status));
    }

    @Operation(summary = "积分订单发货")
    @PostMapping("/orders/{id}/deliver")
    public R<Void> deliver(@PathVariable Long id, @RequestParam String expressName, @RequestParam String expressNo) {
        integralGoodsService.deliver(id, expressName, expressNo);
        return R.ok("发货成功", null);
    }
}