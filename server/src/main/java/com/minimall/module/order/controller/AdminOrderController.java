package com.minimall.module.order.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.order.model.dto.DeliverDTO;
import com.minimall.module.order.model.vo.OrderVO;
import com.minimall.module.order.service.OrderService;
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
 * 后台订单管理
 */
@Tag(name = "后台-订单管理")
@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminOrderController {

    private final OrderService orderService;

    @Operation(summary = "订单列表")
    @GetMapping
    public R<List<OrderVO>> list(@RequestParam(required = false) Integer status,
                                 @RequestParam(required = false) String orderSn) {
        return R.ok(orderService.adminList(status, orderSn));
    }

    @Operation(summary = "发货")
    @PostMapping("/{id}/deliver")
    public R<Void> deliver(@PathVariable Long id, @Valid @RequestBody DeliverDTO dto) {
        orderService.deliver(id, dto.getExpressId(), dto.getExpressName(), dto.getExpressNo());
        return R.ok("发货成功", null);
    }
}
