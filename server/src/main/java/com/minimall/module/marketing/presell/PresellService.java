package com.minimall.module.marketing.presell;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minimall.common.exception.BusinessException;
import com.minimall.common.result.ResultCode;
import com.minimall.module.goods.model.entity.Goods;
import com.minimall.module.goods.model.entity.GoodsItem;
import com.minimall.module.goods.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预售：付定金 → 付尾款（截止前）→ 完成
 */
@Service
@RequiredArgsConstructor
public class PresellService {

    private final PresellActivityMapper activityMapper;
    private final PresellOrderMapper orderMapper;
    private final GoodsService goodsService;

    /** 预售活动列表 */
    public List<PresellActivity> list() {
        LocalDateTime now = LocalDateTime.now();
        return activityMapper.selectList(Wrappers.<PresellActivity>lambdaQuery()
                .eq(PresellActivity::getStatus, 1)
                .le(PresellActivity::getStartTime, now)
                .ge(PresellActivity::getEndTime, now));
    }

    /** 付定金下单 */
    @Transactional(rollbackFor = Exception.class)
    public Long order(Long userId, PresellBuyDTO dto) {
        PresellActivity act = activityMapper.selectById(dto.getActivityId());
        if (act == null || act.getStatus() != 1) throw new BusinessException("预售活动不存在");
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(act.getStartTime()) || now.isAfter(act.getEndTime())) {
            throw new BusinessException("预售不在活动期内");
        }
        if (act.getStock() < 1) throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
        if (StrUtil.isBlank(dto.getContact()) || StrUtil.isBlank(dto.getMobile()) || StrUtil.isBlank(dto.getAddress())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "请填写完整收货信息");
        }
        Goods g = goodsService.getById(act.getGoodsId());

        PresellOrder po = new PresellOrder();
        po.setOrderSn("PS" + IdUtil.getSnowflakeNextIdStr());
        po.setUserId(userId);
        po.setActivityId(act.getId());
        po.setGoodsId(act.getGoodsId());
        po.setGoodsItemId(act.getGoodsItemId());
        po.setGoodsName(g == null ? act.getName() : g.getName());
        po.setGoodsImage(g == null ? "" : g.getImage());
        po.setDeposit(act.getDeposit());
        po.setFinalPrice(act.getFinalPrice());
        po.setStatus(0);
        po.setContact(dto.getContact());
        po.setMobile(dto.getMobile());
        po.setAddress(dto.getAddress());
        orderMapper.insert(po);
        return po.getId();
    }

    /** 支付定金（模拟）→ 锁库存 */
    @Transactional(rollbackFor = Exception.class)
    public void payDeposit(Long userId, Long orderId) {
        PresellOrder po = checkOwner(userId, orderId);
        if (po.getStatus() != 0) throw new BusinessException("订单状态不允许该操作");
        orderMapper.update(null, Wrappers.<PresellOrder>lambdaUpdate()
                .eq(PresellOrder::getId, orderId)
                .set(PresellOrder::getStatus, 1)
                .set(PresellOrder::getPaySn, "PSPAY" + System.currentTimeMillis()));
        // 锁定预售库存
        activityMapper.update(null, Wrappers.<PresellActivity>lambdaUpdate()
                .eq(PresellActivity::getId, po.getActivityId())
                .setSql("stock = stock - 1")
                .setSql("sales = sales + 1"));
    }

    /** 支付尾款（模拟，不超截止时间） */
    @Transactional(rollbackFor = Exception.class)
    public void payFinal(Long userId, Long orderId) {
        PresellOrder po = checkOwner(userId, orderId);
        PresellActivity act = activityMapper.selectById(po.getActivityId());
        if (po.getStatus() != 1) throw new BusinessException("订单状态不允许该操作");
        if (act != null && act.getFinalEndTime() != null && LocalDateTime.now().isAfter(act.getFinalEndTime())) {
            throw new BusinessException("尾款支付已截止");
        }
        orderMapper.update(null, Wrappers.<PresellOrder>lambdaUpdate()
                .eq(PresellOrder::getId, orderId).set(PresellOrder::getStatus, 2));
    }

    /** 我的预售订单 */
    public List<PresellOrder> myOrders(Long userId) {
        return orderMapper.selectList(Wrappers.<PresellOrder>lambdaQuery()
                .eq(PresellOrder::getUserId, userId).orderByDesc(PresellOrder::getId));
    }

    /** 后台预售订单 */
    public List<PresellOrder> adminOrders() {
        return orderMapper.selectList(Wrappers.<PresellOrder>lambdaQuery().orderByDesc(PresellOrder::getId));
    }

    /** 后台发货 */
    public void deliver(Long orderId, String expressName, String expressNo) {
        PresellOrder po = orderMapper.selectById(orderId);
        if (po == null || po.getStatus() != 2) throw new BusinessException("仅已付尾款订单可发货");
        orderMapper.update(null, Wrappers.<PresellOrder>lambdaUpdate()
                .eq(PresellOrder::getId, orderId)
                .set(PresellOrder::getExpressName, expressName)
                .set(PresellOrder::getExpressNo, expressNo));
    }

    private PresellOrder checkOwner(Long userId, Long orderId) {
        PresellOrder po = orderMapper.selectById(orderId);
        if (po == null || !po.getUserId().equals(userId)) throw new BusinessException("预售订单不存在");
        return po;
    }
}