package com.minimall.module.aftersale.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minimall.common.exception.BusinessException;
import com.minimall.common.result.ResultCode;
import com.minimall.module.aftersale.mapper.AfterSaleMapper;
import com.minimall.module.aftersale.model.dto.AfterSaleCreateDTO;
import com.minimall.module.aftersale.model.entity.AfterSale;
import com.minimall.module.goods.service.GoodsService;
import com.minimall.module.order.model.entity.Order;
import com.minimall.module.order.model.entity.OrderGoods;
import com.minimall.module.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AfterSaleService extends ServiceImpl<AfterSaleMapper, AfterSale> {

    private final OrderService orderService;
    private final GoodsService goodsService;

    /** 申请售后 */
    public Long create(Long userId, AfterSaleCreateDTO dto) {
        Order order = orderService.checkOwnerOrder(userId, dto.getOrderId());
        // 仅已支付(待发货/待收货/已完成)可申请售后
        if (order.getOrderStatus() < 1 || order.getOrderStatus() > 3) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        OrderGoods og = orderService.checkOwnerOrderGoods(dto.getOrderId(), dto.getOrderGoodsId());
        if (og.getIsAfterSale() != null && og.getIsAfterSale() == 1) {
            throw new BusinessException("该商品已在售后中或已退款");
        }
        // 校验是否有进行中的售后
        Long exist = this.lambdaQuery()
                .eq(AfterSale::getOrderGoodsId, dto.getOrderGoodsId())
                .in(AfterSale::getStatus, 0, 1, 3)
                .count();
        if (exist != null && exist > 0) {
            throw new BusinessException("该商品已存在售后申请");
        }

        AfterSale as = new AfterSale();
        as.setAfterSaleSn("AS" + IdUtil.getSnowflakeNextIdStr());
        as.setOrderId(dto.getOrderId());
        as.setOrderGoodsId(dto.getOrderGoodsId());
        as.setUserId(userId);
        as.setType(dto.getType());
        as.setReason(dto.getReason());
        as.setRefundAmount(dto.getRefundAmount() != null ? dto.getRefundAmount() : og.getTotalPrice());
        as.setStatus(0);
        this.save(as);

        orderService.lambdaUpdate().eq(Order::getId, order.getId()).set(Order::getIsAfterSale, 1).update();
        return as.getId();
    }

    /** 我的售后列表 */
    public List<AfterSale> myList(Long userId) {
        return this.lambdaQuery().eq(AfterSale::getUserId, userId).orderByDesc(AfterSale::getId).list();
    }

    /** 用户取消售后 */
    public void cancel(Long userId, Long id) {
        AfterSale as = this.getById(id);
        if (as == null || !as.getUserId().equals(userId)) {
            throw new BusinessException("售后单不存在");
        }
        if (as.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        this.lambdaUpdate().eq(AfterSale::getId, id).set(AfterSale::getStatus, 4).update();
        // 若该订单仅此一单售后已关闭，重置订单售后标记（简化：不重置）
    }

    // ---------------- 后台 ----------------

    /** 后台售后列表 */
    public List<AfterSale> adminList(Integer status) {
        return this.lambdaQuery()
                .eq(status != null, AfterSale::getStatus, status)
                .orderByDesc(AfterSale::getId)
                .list();
    }

    /** 同意退款（P2 模拟退款，直接置为已退款） */
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        AfterSale as = this.getById(id);
        if (as == null || as.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        // 退款：恢复库存 + 减销量（模拟）
        OrderGoods og = orderService.listOrderGoods(as.getOrderId()).stream()
                .filter(g -> g.getId().equals(as.getOrderGoodsId()))
                .findFirst().orElse(null);
        if (og != null) {
            goodsService.restoreStock(og.getGoodsItemId(), og.getNum());
            goodsService.minusSales(og.getGoodsId(), og.getNum());
            orderService.updateOrderGoodsAfterSale(as.getOrderGoodsId());
        }
        this.lambdaUpdate().eq(AfterSale::getId, id)
                .set(AfterSale::getStatus, 3)
                .set(AfterSale::getRefundSn, "RF" + System.currentTimeMillis())
                .set(AfterSale::getRefundTime, LocalDateTime.now())
                .update();
    }

    /** 拒绝售后 */
    public void reject(Long id, String reason) {
        AfterSale as = this.getById(id);
        if (as == null || as.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        this.lambdaUpdate().eq(AfterSale::getId, id)
                .set(AfterSale::getStatus, 2)
                .set(AfterSale::getReason, as.getReason() + " | 拒绝原因:" + (reason == null ? "" : reason))
                .update();
    }
}
