package com.minimall.module.order.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.minimall.common.result.R;
import com.minimall.module.order.model.dto.BatchDeliverDTO;
import com.minimall.module.order.model.dto.ChangePriceDTO;
import com.minimall.module.order.model.dto.DeliverDTO;
import com.minimall.module.order.model.dto.TraceAddDTO;
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
    @SaCheckPermission(value = "order:deliver", type = "admin")
    @PostMapping("/{id}/deliver")
    public R<Void> deliver(@PathVariable Long id, @Valid @RequestBody DeliverDTO dto) {
        orderService.deliver(id, dto.getExpressId(), dto.getExpressName(), dto.getExpressNo());
        return R.ok("发货成功", null);
    }

    @Operation(summary = "批量发货")
    @SaCheckPermission(value = "order:deliver", type = "admin")
    @PostMapping("/deliver-batch")
    public R<Integer> deliverBatch(@RequestBody BatchDeliverDTO dto) {
        int n = orderService.batchDeliver(dto.getOrderIds(), dto.getExpressId(), dto.getExpressName(), dto.getExpressNo());
        return R.ok("成功发货 " + n + " 单", n);
    }

    @Operation(summary = "物流轨迹")
    @GetMapping("/{id}/traces")
    public R<List<java.util.Map<String, Object>>> traces(@PathVariable Long id) {
        return R.ok(orderService.adminTraces(id));
    }

    @Operation(summary = "追加物流轨迹")
    @SaCheckPermission(value = "order:deliver", type = "admin")
    @PostMapping("/{id}/trace")
    public R<Void> addTrace(@PathVariable Long id, @RequestBody TraceAddDTO dto) {
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new com.minimall.common.exception.BusinessException("轨迹内容不能为空");
        }
        orderService.addTraceAt(id, dto.getDescription(), "admin", dto.getTraceTime());
        return R.ok("已添加", null);
    }

    @Operation(summary = "模拟推进物流（演示用，按节点顺序走一步）")
    @SaCheckPermission(value = "order:deliver", type = "admin")
    @PostMapping("/{id}/trace/advance")
    public R<String> advanceTrace(@PathVariable Long id) {
        return R.ok(orderService.advanceTrace(id, "admin"));
    }

    @Operation(summary = "订单改价（仅待付款）")
    @SaCheckPermission(value = "order:edit", type = "admin")
    @PostMapping("/{id}/change-price")
    public R<Void> changePrice(@PathVariable Long id, @RequestBody ChangePriceDTO dto) {
        orderService.changePrice(id, dto.getPayAmount(), dto.getReason(), "admin");
        return R.ok("改价成功", null);
    }
}
