package com.minimall.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minimall.module.order.model.entity.Order;
import com.minimall.module.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务：未支付订单超时自动关单。
 *
 * <p>没有这个任务时，待支付订单永远停在 order_status=0，占着库存和优惠券不放；
 * 秒杀场景尤其致命（库存被锁死，别人抢不到，也没人释放）。
 *
 * <p>超时时长分两档：普通单默认 30 分钟，秒杀单默认 5 分钟（改 application.yml 即可）。
 * 用 {@code OrderService.autoCancel} 的 CAS 抢占状态，避免和用户「刚好支付」并发误杀。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    /** 普通订单支付超时（分钟） */
    @Value("${minimall.order.pay-timeout-minutes:30}")
    private int payTimeoutMinutes;

    /** 秒杀订单支付超时（分钟）：秒杀库存金贵，窗口更短 */
    @Value("${minimall.order.seckill-pay-timeout-minutes:5}")
    private int seckillPayTimeoutMinutes;

    private final OrderService orderService;

    /** 每分钟扫一次 */
    @Scheduled(fixedDelay = 60_000, initialDelay = 45_000)
    public void scan() {
        int total = 0;
        try {
            total += closeExpired(false, payTimeoutMinutes, "超时未支付，系统自动关闭");
            total += closeExpired(true, seckillPayTimeoutMinutes, "秒杀订单超时未支付，系统自动关闭");
        } catch (Exception e) {
            log.error("未支付订单超时关单扫描异常", e);
        }
        if (total > 0) log.info("未支付订单超时关单完成，共关闭 {} 单", total);
    }

    private int closeExpired(boolean seckill, int minutes, String reason) {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(minutes);
        List<Order> list = orderService.list(Wrappers.<Order>lambdaQuery()
                .eq(Order::getOrderStatus, 0)
                .eq(Order::getPayStatus, 0)
                .lt(Order::getCreateTime, deadline)
                .eq(seckill, Order::getOrderType, 2)
                // 非秒杀分支要排除秒杀单，否则会被 30 分钟那档重复扫到
                .ne(!seckill, Order::getOrderType, 2)
                .last("limit 200"));
        int closed = 0;
        for (Order o : list) {
            try {
                if (orderService.autoCancel(o.getId(), reason)) closed++;
            } catch (Exception e) {
                log.warn("订单 {} 自动关单失败: {}", o.getId(), e.getMessage());
            }
        }
        return closed;
    }
}
