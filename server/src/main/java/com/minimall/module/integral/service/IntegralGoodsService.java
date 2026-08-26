package com.minimall.module.integral.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minimall.common.exception.BusinessException;
import com.minimall.common.result.ResultCode;
import com.minimall.module.integral.mapper.IntegralGoodsMapper;
import com.minimall.module.integral.mapper.IntegralOrderMapper;
import com.minimall.module.integral.model.entity.IntegralGoods;
import com.minimall.module.integral.model.entity.IntegralOrder;
import com.minimall.module.integral.model.dto.ExchangeDTO;
import com.minimall.module.user.model.entity.User;
import com.minimall.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IntegralGoodsService extends ServiceImpl<IntegralGoodsMapper, IntegralGoods> {

    private final IntegralOrderMapper integralOrderMapper;
    private final UserService userService;

    /** 前台列表（上架） */
    public List<IntegralGoods> shopList() {
        return this.lambdaQuery().eq(IntegralGoods::getStatus, 1).orderByDesc(IntegralGoods::getId).list();
    }

    /** 前台详情 */
    public IntegralGoods detail(Long id) {
        IntegralGoods g = this.getById(id);
        if (g == null) throw new BusinessException(ResultCode.GOODS_NOT_EXIST);
        return g;
    }

    /** 积分兑换 */
    @Transactional(rollbackFor = Exception.class)
    public Long exchange(Long userId, ExchangeDTO dto) {
        IntegralGoods g = this.getById(dto.getGoodsId());
        if (g == null || g.getStatus() != 1) throw new BusinessException(ResultCode.GOODS_NOT_EXIST);
        int num = dto.getNum() == null ? 1 : dto.getNum();
        if (num < 1) num = 1;
        if (g.getStock() < num) throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
        if (StrUtil.isBlank(dto.getContact()) || StrUtil.isBlank(dto.getMobile()) || StrUtil.isBlank(dto.getAddress())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "请填写完整收货信息");
        }
        long need = g.getIntegral() * num;
        User user = userService.getById(userId);
        if (user == null || user.getIntegral() == null || user.getIntegral() < need) {
            throw new BusinessException("积分不足");
        }
        // 扣积分 + 减库存 + 加销量
        userService.update(Wrappers.<User>lambdaUpdate().eq(User::getId, userId).setSql("integral = integral - " + need));

        IntegralOrder order = new IntegralOrder();
        order.setOrderSn("IG" + IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setIntegralGoodsId(g.getId());
        order.setGoodsName(g.getName());
        order.setGoodsImage(g.getImage());
        order.setIntegral(g.getIntegral());
        order.setNum(num);
        order.setTotalIntegral(need);
        order.setStatus(0);
        order.setContact(dto.getContact());
        order.setMobile(dto.getMobile());
        order.setAddress(dto.getAddress());
        integralOrderMapper.insert(order);

        this.update(Wrappers.<IntegralGoods>lambdaUpdate()
                .eq(IntegralGoods::getId, g.getId())
                .setSql("stock = stock - " + num)
                .setSql("sales = sales + " + num));
        return order.getId();
    }

    /** 我的兑换订单 */
    public List<IntegralOrder> myOrders(Long userId) {
        return integralOrderMapper.selectList(Wrappers.<IntegralOrder>lambdaQuery()
                .eq(IntegralOrder::getUserId, userId).orderByDesc(IntegralOrder::getId));
    }

    /** 后台订单列表 */
    public List<IntegralOrder> adminOrders(Integer status) {
        return integralOrderMapper.selectList(Wrappers.<IntegralOrder>lambdaQuery()
                .eq(status != null, IntegralOrder::getStatus, status).orderByDesc(IntegralOrder::getId));
    }

    /** 后台发货 */
    public void deliver(Long orderId, String expressName, String expressNo) {
        IntegralOrder order = integralOrderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 0) throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        integralOrderMapper.update(null, Wrappers.<IntegralOrder>lambdaUpdate()
                .eq(IntegralOrder::getId, orderId)
                .set(IntegralOrder::getStatus, 1)
                .set(IntegralOrder::getExpressName, expressName)
                .set(IntegralOrder::getExpressNo, expressNo)
                .set(IntegralOrder::getExpressTime, LocalDateTime.now()));
    }
}