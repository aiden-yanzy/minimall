package com.minimall.module.integral.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.minimall.common.result.R;
import com.minimall.module.integral.model.dto.ExchangeDTO;
import com.minimall.module.integral.model.entity.IntegralGoods;
import com.minimall.module.integral.model.entity.IntegralOrder;
import com.minimall.module.integral.service.IntegralGoodsService;
import com.minimall.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端积分商城
 */
@Tag(name = "C端-积分商城")
@RestController
@RequestMapping("/shop/integral")
@RequiredArgsConstructor
public class IntegralGoodsController {

    private final IntegralGoodsService integralGoodsService;
    private final UserService userService;

    @Operation(summary = "积分商品列表")
    @SaIgnore
    @GetMapping("/goods")
    public R<List<IntegralGoods>> goodsList() {
        return R.ok(integralGoodsService.shopList());
    }

    @Operation(summary = "积分兑换")
    @SaCheckLogin(type = "user")
    @PostMapping("/exchange")
    public R<Long> exchange(@Valid @RequestBody ExchangeDTO dto) {
        return R.ok("兑换成功", integralGoodsService.exchange(userService.getCurrentUserId(), dto));
    }

    @Operation(summary = "我的兑换订单")
    @SaCheckLogin(type = "user")
    @GetMapping("/orders")
    public R<List<IntegralOrder>> orders() {
        return R.ok(integralGoodsService.myOrders(userService.getCurrentUserId()));
    }
}