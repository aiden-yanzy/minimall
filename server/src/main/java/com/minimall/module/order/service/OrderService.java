package com.minimall.module.order.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minimall.common.exception.BusinessException;
import com.minimall.common.result.ResultCode;
import com.minimall.module.cart.model.entity.Cart;
import com.minimall.module.cart.service.CartService;
import com.minimall.module.goods.model.entity.Goods;
import com.minimall.module.goods.model.entity.GoodsItem;
import com.minimall.module.goods.service.GoodsService;
import com.minimall.module.order.mapper.OrderGoodsMapper;
import com.minimall.module.order.mapper.OrderMapper;
import com.minimall.module.order.model.dto.OrderCreateDTO;
import com.minimall.module.order.model.entity.Order;
import com.minimall.module.order.model.entity.OrderGoods;
import com.minimall.module.order.model.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService extends ServiceImpl<OrderMapper, Order> {

    private final OrderGoodsMapper orderGoodsMapper;
    private final GoodsService goodsService;
    private final CartService cartService;

    /** 内部结算行 */
    private static class Line {
        GoodsItem item;
        Goods goods;
        int num;
    }

    /** 创建订单（从购物车勾选或立即购买） */
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, OrderCreateDTO dto) {
        // 1. 组装结算行
        List<Line> lines = assembleLines(userId, dto);

        // 2. 校验库存
        for (Line l : lines) {
            if (l.item.getStock() == null || l.item.getStock() < l.num) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
            }
            if (l.goods == null || l.goods.getStatus() == null || l.goods.getStatus() != 1) {
                throw new BusinessException(ResultCode.GOODS_OFF_SHELF);
            }
        }

        // 3. 计算金额
        long total = lines.stream().mapToLong(l -> l.item.getPrice() * l.num).sum();

        // 4. 创建订单
        Order order = new Order();
        order.setOrderSn("MM" + IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setOrderType(0);
        order.setOrderStatus(0);
        order.setPayStatus(0);
        order.setPayType(dto.getPayType());
        order.setTotalAmount(total);
        order.setDiscountAmount(0L);
        order.setFreightAmount(0L);
        order.setPayAmount(total);
        order.setRemark(dto.getRemark());
        order.setContact(dto.getContact());
        order.setMobile(dto.getMobile());
        order.setAddress(dto.getAddress());
        order.setDeliveryType(1);
        this.save(order);

        // 5. 写订单商品 + 扣库存 + 增加销量
        for (Line l : lines) {
            OrderGoods og = new OrderGoods();
            og.setOrderId(order.getId());
            og.setUserId(userId);
            og.setGoodsId(l.goods.getId());
            og.setGoodsItemId(l.item.getId());
            og.setGoodsName(l.goods.getName());
            og.setGoodsImage(l.goods.getImage());
            og.setSpecValueStr(l.item.getSpecValueStr());
            og.setPrice(l.item.getPrice());
            og.setNum(l.num);
            og.setTotalPrice(l.item.getPrice() * l.num);
            og.setDiscountPrice(0L);
            orderGoodsMapper.insert(og);

            // 扣 SKU 库存、加销量
            goodsService.deductStock(l.item.getId(), l.num);
            // 扣 SPU 库存、加销量
            goodsService.addSales(l.goods.getId(), l.num);
        }

        // 6. 清空已勾选购物车
        if ("cart".equals(dto.getBuyType())) {
            cartService.removeSelected(userId);
        }
        return order.getId();
    }

    private List<Line> assembleLines(Long userId, OrderCreateDTO dto) {
        List<Line> result = new ArrayList<>();
        if ("buyNow".equals(dto.getBuyType())) {
            GoodsItem item = goodsService.getGoodsItem(dto.getGoodsItemId());
            if (item == null) throw new BusinessException(ResultCode.GOODS_NOT_EXIST);
            Line l = new Line();
            l.item = item;
            l.goods = goodsService.getById(item.getGoodsId());
            l.num = dto.getNum() == null ? 1 : dto.getNum();
            result.add(l);
        } else {
            List<Cart> carts = cartService.lambdaQuery()
                    .eq(Cart::getUserId, userId)
                    .eq(Cart::getIsSelected, 1)
                    .list();
            for (Cart c : carts) {
                GoodsItem item = goodsService.getGoodsItem(c.getGoodsItemId());
                if (item == null) throw new BusinessException("已失效的商品，请重新选购");
                Line l = new Line();
                l.item = item;
                l.goods = goodsService.getById(item.getGoodsId());
                l.num = c.getNum();
                result.add(l);
            }
        }
        if (result.isEmpty()) throw new BusinessException("请先选择商品");
        return result;
    }

    /** 订单列表 */
    public List<OrderVO> list(Long userId, Integer status) {
        List<Order> orders = this.lambdaQuery()
                .eq(Order::getUserId, userId)
                .eq(status != null, Order::getOrderStatus, status)
                .orderByDesc(Order::getId)
                .list();
        return orders.stream().map(o -> {
            OrderVO vo = OrderVO.from(o);
            vo.setGoodsList(loadGoods(o.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    /** 订单详情 */
    public OrderVO detail(Long userId, Long id) {
        Order order = this.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        OrderVO vo = OrderVO.from(order);
        vo.setGoodsList(loadGoods(order.getId()));
        return vo;
    }

    private List<OrderVO.OrderGoodsVO> loadGoods(Long orderId) {
        return orderGoodsMapper.selectList(Wrappers.<OrderGoods>lambdaQuery().eq(OrderGoods::getOrderId, orderId))
                .stream().map(OrderVO.OrderGoodsVO::from).collect(Collectors.toList());
    }

    /** 取消订单（恢复库存） */
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, Long id, String reason) {
        Order order = this.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        if (order.getOrderStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        // 未支付才允许取消；恢复库存
        List<OrderGoods> ogs = orderGoodsMapper.selectList(
                Wrappers.<OrderGoods>lambdaQuery().eq(OrderGoods::getOrderId, id));
        for (OrderGoods og : ogs) {
            goodsService.restoreStock(og.getGoodsItemId(), og.getNum());
            goodsService.minusSales(og.getGoodsId(), og.getNum());
        }
        this.lambdaUpdate().eq(Order::getId, id)
                .set(Order::getOrderStatus, 4)
                .set(Order::getCancelTime, LocalDateTime.now())
                .set(Order::getCancelReason, reason)
                .update();
    }

    /** 由支付模块调用：标记已支付 */
    @Transactional(rollbackFor = Exception.class)
    public void markPaid(String orderSn, Integer payType, String paySn) {
        Order order = this.lambdaQuery().eq(Order::getOrderSn, orderSn).one();
        if (order == null) throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        if (order.getOrderStatus() != 0) throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        this.lambdaUpdate().eq(Order::getId, order.getId())
                .set(Order::getPayStatus, 1)
                .set(Order::getOrderStatus, 1)
                .set(Order::getPayType, payType)
                .set(Order::getPaySn, paySn)
                .set(Order::getPayTime, LocalDateTime.now())
                .update();
    }

    /** 后台订单分页（含商品） */
    public List<OrderVO> adminList(Integer status, String orderSn) {
        return this.lambdaQuery()
                .eq(status != null, Order::getOrderStatus, status)
                .like(orderSn != null, Order::getOrderSn, orderSn)
                .orderByDesc(Order::getId)
                .list()
                .stream().map(o -> {
                    OrderVO vo = OrderVO.from(o);
                    vo.setGoodsList(loadGoods(o.getId()));
                    return vo;
                }).collect(Collectors.toList());
    }

    /** 确认收货 */
    public void confirmReceive(Long userId, Long orderId) {
        Order order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        if (order.getOrderStatus() != 2) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        this.lambdaUpdate().eq(Order::getId, orderId)
                .set(Order::getOrderStatus, 3)
                .set(Order::getReceiveTime, LocalDateTime.now())
                .update();
    }

    /** 后台发货 */
    public void deliver(Long orderId, Long expressId, String expressName, String expressNo) {
        Order order = this.getById(orderId);
        if (order == null) throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        if (order.getOrderStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        this.lambdaUpdate().eq(Order::getId, orderId)
                .set(Order::getOrderStatus, 2)
                .set(Order::getExpressId, expressId)
                .set(Order::getExpressName, expressName)
                .set(Order::getExpressNo, expressNo)
                .set(Order::getDeliveryTime, LocalDateTime.now())
                .update();
    }

    /** 订单商品行（供售后使用） */
    public List<OrderGoods> listOrderGoods(Long orderId) {
        return orderGoodsMapper.selectList(Wrappers.<OrderGoods>lambdaQuery().eq(OrderGoods::getOrderId, orderId));
    }

    /** 校验订单归属，返回订单 */
    public Order checkOwnerOrder(Long userId, Long orderId) {
        Order order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        return order;
    }

    /** 订单行归属校验 */
    public OrderGoods checkOwnerOrderGoods(Long orderId, Long orderGoodsId) {
        OrderGoods og = orderGoodsMapper.selectById(orderGoodsId);
        if (og == null || !og.getOrderId().equals(orderId)) {
            throw new BusinessException("订单商品不存在");
        }
        return og;
    }

    /** 标记订单商品已售后 */
    public void updateOrderGoodsAfterSale(Long orderGoodsId) {
        orderGoodsMapper.update(null, Wrappers.<OrderGoods>lambdaUpdate()
                .eq(OrderGoods::getId, orderGoodsId)
                .set(OrderGoods::getIsAfterSale, 1));
    }
}
