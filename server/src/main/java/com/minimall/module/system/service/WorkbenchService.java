package com.minimall.module.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minimall.module.aftersale.mapper.AfterSaleMapper;
import com.minimall.module.goods.mapper.GoodsMapper;
import com.minimall.module.order.mapper.OrderMapper;
import com.minimall.module.order.model.entity.Order;
import com.minimall.module.user.mapper.UserMapper;
import com.minimall.module.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 工作台数据报表
 */
@Service
@RequiredArgsConstructor
public class WorkbenchService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final GoodsMapper goodsMapper;
    private final AfterSaleMapper afterSaleMapper;

    public Map<String, Object> stats() {
        Map<String, Object> m = new HashMap<>();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);

        Long orderCount = orderMapper.selectCount(Wrappers.<Order>lambdaQuery().ge(Order::getCreateTime, todayStart).lt(Order::getCreateTime, todayEnd));
        Long paidToday = orderMapper.selectCount(Wrappers.<Order>lambdaQuery()
                .ge(Order::getCreateTime, todayStart).lt(Order::getCreateTime, todayEnd)
                .eq(Order::getPayStatus, 1));

        m.put("todayOrder", orderCount == null ? 0 : orderCount);
        m.put("todayPaidOrder", paidToday == null ? 0 : paidToday);
        m.put("pendingDelivery", orderMapper.selectCount(Wrappers.<Order>lambdaQuery().eq(Order::getOrderStatus, 1)));
        m.put("pendingAfterSale", afterSaleMapper.selectCount(Wrappers.<com.minimall.module.aftersale.model.entity.AfterSale>lambdaQuery()
                .eq(com.minimall.module.aftersale.model.entity.AfterSale::getStatus, 0)));
        m.put("userCount", userMapper.selectCount(null));
        m.put("goodsCount", goodsMapper.selectCount(null));

        // 近7日订单趋势（按日聚合）
        Map<String, Long> trend = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            Long c = orderMapper.selectCount(Wrappers.<Order>lambdaQuery()
                    .ge(Order::getCreateTime, d.atStartOfDay())
                    .lt(Order::getCreateTime, d.plusDays(1).atStartOfDay()));
            trend.put(d.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")), c == null ? 0 : c);
        }
        m.put("trend", trend);
        return m;
    }
}