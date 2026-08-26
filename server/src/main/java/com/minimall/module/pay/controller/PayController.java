package com.minimall.module.pay.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.order.service.OrderService;
import com.minimall.module.pay.model.dto.MockPayDTO;
import com.minimall.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模拟支付网关（P1）
 * 预留微信/支付宝真实回调接口结构，后续无痛替换。
 */
@Tag(name = "C端-模拟支付")
@RestController
@RequestMapping("/shop/pay")
@RequiredArgsConstructor
@SaCheckLogin(type = "user")
public class PayController {

    private final OrderService orderService;
    private final UserService userService;

    @Operation(summary = "模拟支付（直接标记订单已支付）")
    @PostMapping("/mock")
    public R<Void> mock(@Valid @RequestBody MockPayDTO dto) {
        // 校验订单归属
        Long userId = userService.getCurrentUserId();
        String paySn = "MOCK" + System.currentTimeMillis();
        orderService.markPaid(dto.getOrderSn(), dto.getPayType(), paySn);
        return R.ok();
    }
}
