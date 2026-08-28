package com.minimall.module.freeshipping.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minimall.module.freeshipping.mapper.FreeShippingMapper;
import com.minimall.module.freeshipping.mapper.FreeShippingOrderMapper;
import com.minimall.module.freeshipping.model.entity.FreeShipping;
import com.minimall.module.freeshipping.model.entity.FreeShippingOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 满额免邮
 */
@Service
@RequiredArgsConstructor
public class FreeShippingService extends ServiceImpl<FreeShippingMapper, FreeShipping> {

    private final FreeShippingOrderMapper orderMapper;

    /** 当前生效的免邮规则 */
    public Optional<FreeShipping> active() {
        LocalDateTime now = LocalDateTime.now();
        return this.lambdaQuery()
                .eq(FreeShipping::getStatus, 1)
                .and(q -> q.isNull(FreeShipping::getStartTime).or(w -> w.le(FreeShipping::getStartTime, now)))
                .and(q -> q.isNull(FreeShipping::getEndTime).or(w -> w.ge(FreeShipping::getEndTime, now)))
                .orderByDesc(FreeShipping::getId).last("limit 1").oneOpt();
    }

    /** 是否命中免邮：订单应付(不含运费) >= 门槛 */
    public boolean hit(long amountWithoutFreight) {
        return active().map(f -> amountWithoutFreight >= f.getThreshold()).orElse(false);
    }

    /** 记录免邮订单 */
    public void record(Long userId, Long orderId, long discount) {
        FreeShippingOrder o = new FreeShippingOrder();
        o.setUserId(userId);
        o.setOrderId(orderId);
        o.setDiscountAmount(discount);
        orderMapper.insert(o);
    }

    public List<FreeShipping> rules() {
        return this.list(Wrappers.<FreeShipping>lambdaQuery().orderByDesc(FreeShipping::getId));
    }
}