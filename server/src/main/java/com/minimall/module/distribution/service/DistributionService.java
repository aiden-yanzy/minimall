package com.minimall.module.distribution.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minimall.common.exception.BusinessException;
import com.minimall.module.distribution.mapper.DistributionLevelMapper;
import com.minimall.module.distribution.mapper.DistributionOrderGoodsMapper;
import com.minimall.module.distribution.model.entity.DistributionLevel;
import com.minimall.module.distribution.model.entity.DistributionOrderGoods;
import com.minimall.module.order.mapper.OrderGoodsMapper;
import com.minimall.module.order.model.entity.Order;
import com.minimall.module.order.model.entity.OrderGoods;
import com.minimall.module.user.model.entity.User;
import com.minimall.module.user.service.UserService;
import com.minimall.module.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分销：绑定推荐关系 + 收货后给推荐人结算一级佣金
 */
@Service
@RequiredArgsConstructor
public class DistributionService {

    private final DistributionLevelMapper levelMapper;
    private final DistributionOrderGoodsMapper dogMapper;
    private final OrderGoodsMapper orderGoodsMapper;
    private final UserService userService;
    private final WalletService walletService;

    /** 绑定推荐人（只能一次，不可绑定自己） */
    public void bind(Long userId, Long referrerId) {
        if (referrerId == null || referrerId.equals(userId)) throw new BusinessException("推荐人无效");
        User user = userService.getById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (user.getReferrerId() != null) throw new BusinessException("已绑定推荐人，不能修改");
        userService.update(Wrappers.<User>lambdaUpdate().eq(User::getId, userId).set(User::getReferrerId, referrerId));
    }

    /** 我的推荐人 */
    public User referrer(Long userId) {
        User me = userService.getById(userId);
        if (me == null || me.getReferrerId() == null) return null;
        return userService.getById(me.getReferrerId());
    }

    /** 我的佣金汇总 */
    public long myCommission(Long userId) {
        List<DistributionOrderGoods> list = listSettled(userId);
        return list.stream().mapToLong(DistributionOrderGoods::getCommission).sum();
    }

    public List<DistributionOrderGoods> listSettled(Long userId) {
        return dogMapper.selectList(Wrappers.<DistributionOrderGoods>lambdaQuery()
                .eq(DistributionOrderGoods::getSellerUserId, userId)
                .eq(DistributionOrderGoods::getStatus, 1)
                .orderByDesc(DistributionOrderGoods::getId));
    }

    /** 确认收货后结算佣金（一级：推荐人按订单实付×费率） */
    @Transactional(rollbackFor = Exception.class)
    public void settle(Long orderId) {
        Order order = null;
        // 通过订单商品倒查买家
        List<OrderGoods> ogs = orderGoodsMapper.selectList(Wrappers.<OrderGoods>lambdaQuery().eq(OrderGoods::getOrderId, orderId));
        if (ogs.isEmpty()) return;
        User buyer = userService.getById(ogs.get(0).getUserId());
        if (buyer == null || buyer.getReferrerId() == null) return;
        DistributionLevel level = levelMapper.selectOne(Wrappers.<DistributionLevel>lambdaQuery()
                .eq(DistributionLevel::getStatus, 1).orderByAsc(DistributionLevel::getId).last("limit 1"));
        int rate = level == null ? 0 : level.getCommissionRate();

        for (OrderGoods og : ogs) {
            if (og.getIsAfterSale() != null && og.getIsAfterSale() == 1) continue;
            long commission = og.getTotalPrice() * (1000 - 0) / 1000; // 按商品小计
            commission = commission * rate / 1000;
            // 不重复结算
            if (dogMapper.selectCount(Wrappers.<DistributionOrderGoods>lambdaQuery()
                    .eq(DistributionOrderGoods::getOrderGoodsId, og.getId())) > 0) continue;
            DistributionOrderGoods dog = new DistributionOrderGoods();
            dog.setOrderId(orderId);
            dog.setOrderGoodsId(og.getId());
            dog.setBuyerUserId(buyer.getId());
            dog.setSellerUserId(buyer.getReferrerId());
            dog.setGoodsAmount(og.getTotalPrice());
            dog.setRate(rate);
            dog.setCommission(commission);
            dog.setStatus(1);
            dogMapper.insert(dog);
            walletService.changeBalance(buyer.getReferrerId(), commission, 5, "分销佣金：订单" + orderId);
        }
    }
}