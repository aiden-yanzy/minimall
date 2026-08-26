package com.minimall.module.order.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.order.model.dto.OrderCreateDTO;
import com.minimall.module.order.model.vo.OrderVO;
import com.minimall.module.order.service.OrderService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端订单
 */
@Tag(name = "C端-订单")
@RestController
@RequestMapping("/shop/order")
@RequiredArgsConstructor
@SaCheckLogin(type = "user")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @Operation(summary = "创建订单")
    @PostMapping("/create")
    public R<Long> create(@Valid @RequestBody OrderCreateDTO dto) {
        return R.ok("下单成功", orderService.create(userService.getCurrentUserId(), dto));
    }

    @Operation(summary = "订单列表")
    @GetMapping
    public R<List<OrderVO>> list(@RequestParam(required = false) Integer status) {
        return R.ok(orderService.list(userService.getCurrentUserId(), status));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public R<OrderVO> detail(@PathVariable Long id) {
        return R.ok(orderService.detail(userService.getCurrentUserId(), id));
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id, @RequestParam(required = false) String reason) {
        orderService.cancel(userService.getCurrentUserId(), id, reason);
        return R.ok();
    }

    @Operation(summary = "确认收货")
    @PostMapping("/{id}/receive")
    public R<Void> receive(@PathVariable Long id) {
        orderService.confirmReceive(userService.getCurrentUserId(), id);
        return R.ok("确认收货成功", null);
    }
}
