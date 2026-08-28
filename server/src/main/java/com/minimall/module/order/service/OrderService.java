package com.minimall.module.order.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minimall.common.exception.BusinessException;
import com.minimall.common.result.ResultCode;
import com.minimall.module.cart.model.entity.Cart;
import com.minimall.module.cart.service.CartService;
import com.minimall.module.distribution.service.DistributionService;
import com.minimall.module.goods.model.entity.Goods;
import com.minimall.module.goods.model.entity.GoodsItem;
import com.minimall.module.goods.service.GoodsService;
import com.minimall.module.marketing.model.entity.CouponList;
import com.minimall.module.marketing.model.entity.FullReduction;
import com.minimall.module.marketing.service.CouponService;
import com.minimall.module.marketing.service.FullReductionService;
import com.minimall.module.order.mapper.OrderGoodsMapper;
import com.minimall.module.order.mapper.OrderMapper;
import com.minimall.module.order.model.dto.OrderCreateDTO;
import com.minimall.module.order.model.entity.Order;
import com.minimall.module.order.model.entity.OrderGoods;
import com.minimall.module.order.model.vo.OrderPreviewVO;
import com.minimall.module.order.model.vo.OrderVO;
import com.minimall.module.seckill.model.entity.SeckillGoods;
import com.minimall.module.seckill.service.SeckillService;
import com.minimall.module.user.model.entity.User;
import com.minimall.module.user.model.entity.UserLevel;
import com.minimall.module.user.service.UserLevelService;
import com.minimall.module.user.service.UserService;
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
    private final CouponService couponService;
    private final FullReductionService fullReductionService;
    private final UserLevelService userLevelService;
    private final UserService userService;
    private final SeckillService seckillService;
    private final DistributionService distributionService;
    private final com.minimall.module.user.mapper.UserAddressMapper userAddressMapper;
    private final com.minimall.module.freight.service.FreightService freightService;
    private final com.minimall.module.freeshipping.service.FreeShippingService freeShippingService;

    /** 内部结算行 */
    private static class Line {
        GoodsItem item;
        Goods goods;
        int num;
        long price; // 实际成交单价（秒杀时为秒杀价）
    }

    /** 创建订单（购物车/立即购买/秒杀） */
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long userId, OrderCreateDTO dto) {
        // 0. 秒杀：预扣 Redis 库存并校验，构造秒杀结算行
        SeckillGoods seckillGoods = null;
        if (dto.getSeckillGoodsId() != null) {
            int n = dto.getNum() == null ? 1 : dto.getNum();
            seckillGoods = seckillService.validateForBuy(dto.getSeckillGoodsId(), userId, n);
        }

        // 1. 组装结算行并校验
        List<Line> lines;
        try {
            lines = assembleLines(userId, dto);
        } catch (RuntimeException e) {
            if (seckillGoods != null) seckillService.rollback(seckillGoods.getId(), dto.getNum());
            throw e;
        }
        for (Line l : lines) {
            if (l.item.getStock() == null || l.item.getStock() < l.num) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
            }
            if (l.goods == null || l.goods.getStatus() == null || l.goods.getStatus() != 1) {
                throw new BusinessException(ResultCode.GOODS_OFF_SHELF);
            }
        }
        // 收货信息：优先地址簿，否则手填必填
        String contact = dto.getContact(), mobile = dto.getMobile(), address = dto.getAddress();
        if (dto.getAddressId() != null) {
            var addr = userAddressMapper.selectById(dto.getAddressId());
            if (addr == null || !addr.getUserId().equals(userId)) throw new BusinessException("收货地址不存在");
            contact = addr.getContact();
            mobile = addr.getMobile();
            address = addr.getAddress();
        }
        if (cn.hutool.core.util.StrUtil.isBlank(contact) || cn.hutool.core.util.StrUtil.isBlank(mobile)
                || cn.hutool.core.util.StrUtil.isBlank(address)) {
            if (seckillGoods != null) seckillService.rollback(seckillGoods.getId(), dto.getNum());
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "请填写完整收货信息");
        }

        // 2. 计算金额（商品总额 + 会员价 + 满减 + 优惠券）
        long total = lines.stream().mapToLong(l -> l.price * l.num).sum();
        PromoResult promo = computePromo(userId, total, dto.getCouponListId());
        // 运费
        long freight = calcFreight(lines);
        boolean freeShip = freeShippingService.hit(promo.payAmount);
        if (freeShip) freight = 0;

        // 3. 创建订单
        Order order = new Order();
        order.setOrderSn("MM" + IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setOrderType(seckillGoods != null ? 2 : 0);
        order.setOrderStatus(0);
        order.setPayStatus(0);
        order.setPayType(dto.getPayType());
        order.setTotalAmount(total);
        order.setMemberDiscount(promo.memberDiscount);
        order.setDiscountAmount(promo.discountAmount());
        order.setFreightAmount(freight);
        order.setCouponId(promo.coupon != null ? promo.coupon.getCouponId() : null);
        order.setCouponAmount(promo.couponAmount);
        order.setPayAmount(promo.payAmount + freight);
        order.setRemark(dto.getRemark());
        order.setContact(contact);
        order.setMobile(mobile);
        order.setAddress(address);
        order.setDeliveryType(1);
        this.save(order);

        // 4. 核销优惠券
        if (promo.coupon != null) {
            couponService.useCoupon(userId, promo.coupon.getId(), order.getId(), total - promo.memberDiscount - promo.fullReduction);
        }

        // 5. 写订单商品；秒杀不扣普通库存，落库扣秒杀库存
        for (Line l : lines) {
            OrderGoods og = new OrderGoods();
            og.setOrderId(order.getId());
            og.setUserId(userId);
            og.setGoodsId(l.goods.getId());
            og.setGoodsItemId(l.item.getId());
            og.setGoodsName(l.goods.getName());
            og.setGoodsImage(l.goods.getImage());
            og.setSpecValueStr(l.item.getSpecValueStr());
            og.setPrice(l.price);
            og.setNum(l.num);
            og.setTotalPrice(l.price * l.num);
            og.setDiscountPrice(0L);
            orderGoodsMapper.insert(og);

            if (seckillGoods == null) {
                goodsService.deductStock(l.item.getId(), l.num);
                goodsService.addSales(l.goods.getId(), l.num);
            }
        }
        if (seckillGoods != null) {
            seckillService.commitDeduct(seckillGoods.getId(), dto.getNum());
        }

        // 6. 记录免邮（若命中）
        if (freeShip) {
            freeShippingService.record(userId, order.getId(), freight);
        }

        // 7. 清空已勾选购物车
        if ("cart".equals(dto.getBuyType())) {
            cartService.removeSelected(userId);
        }
        return order.getId();
    }

    /** 秒杀购买专用开关 */
    private boolean isSeckill(Order order) {
        return order.getOrderType() != null && order.getOrderType() == 2;
    }

    /** 下单预览（无副作用）：金额明细 + 可用优惠券 */
    public OrderPreviewVO preview(Long userId, OrderCreateDTO dto) {
        List<Line> lines = assembleLines(userId, dto);
        long total = lines.stream().mapToLong(l -> l.price * l.num).sum();
        PromoResult promo = computePromo(userId, total, dto.getCouponListId());
        long freight = calcFreight(lines);
        boolean freeShip = freeShippingService.hit(promo.payAmount);
        if (freeShip) freight = 0;

        OrderPreviewVO vo = new OrderPreviewVO();
        vo.setTotalAmount(total);
        vo.setMemberDiscount(promo.memberDiscount);
        vo.setFullReduction(promo.fullReduction);
        vo.setCouponAmount(promo.couponAmount);
        vo.setDiscountAmount(promo.discountAmount());
        vo.setFreightAmount(freight);
        vo.setPayAmount(promo.payAmount + freight);
        vo.setAvailableCoupons(couponService.usable(userId, total - promo.memberDiscount - promo.fullReduction));
        // 会员等级名
        User user = userService.getById(userId);
        if (user != null && user.getLevelId() != null) {
            UserLevel level = userLevelService.getById(user.getLevelId());
            vo.setMemberName(level != null ? level.getName() : null);
        }
        return vo;
    }

    /** 计算运费（订单所有结算行按模板计费累加；由调用方决定是否免邮） */
    private long calcFreight(List<Line> lines) {
        long freight = 0;
        for (Line l : lines) {
            java.math.BigDecimal weight = l.item.getWeight() == null ? java.math.BigDecimal.ZERO : l.item.getWeight();
            freight += freightService.calculate(l.goods, l.num, weight.multiply(java.math.BigDecimal.valueOf(l.num)));
        }
        return freight;
    }

    /** 促销计算结果 */
    private static class PromoResult {
        long memberDiscount = 0;
        long fullReduction = 0;
        long couponAmount = 0;
        long payAmount;
        CouponList coupon;

        long discountAmount() {
            return memberDiscount + fullReduction;
        }
    }

    /** 计算促销优惠（会员价 → 满减 → 优惠券） */
    private PromoResult computePromo(Long userId, long total, Long couponListId) {
        PromoResult r = new PromoResult();
        User user = userService.getById(userId);
        // 会员价
        long payable = total;
        if (user != null) {
            java.math.BigDecimal disc = userLevelService.discountOf(user.getLevelId());
            long memberPay = new java.math.BigDecimal(total).multiply(disc).longValue();
            r.memberDiscount = Math.max(0, total - memberPay);
            payable = total - r.memberDiscount;
        }
        // 满减
        FullReduction fr = fullReductionService.currentActive().orElse(null);
        if (fr != null && payable >= fr.getThreshold()) {
            r.fullReduction = Math.min(fr.getReduce(), payable);
            payable -= r.fullReduction;
        }
        // 优惠券
        if (couponListId != null) {
            CouponList cl = couponService.validateCoupon(userId, couponListId, payable);
            r.coupon = cl;
            r.couponAmount = CouponService.calcDiscount(cl.getType(), cl.getDiscountValue(), payable);
            payable -= r.couponAmount;
        }
        r.payAmount = payable;
        return r;
    }

    private List<Line> assembleLines(Long userId, OrderCreateDTO dto) {
        List<Line> result = new ArrayList<>();
        if (dto.getSeckillGoodsId() != null) {
            SeckillGoods sg = seckillService.getById(dto.getSeckillGoodsId());
            if (sg == null) throw new BusinessException("秒杀商品不存在");
            GoodsItem item = goodsService.getGoodsItem(sg.getGoodsItemId());
            if (item == null) throw new BusinessException(ResultCode.GOODS_NOT_EXIST);
            Line l = new Line();
            l.item = item;
            l.goods = goodsService.getById(item.getGoodsId());
            l.num = dto.getNum() == null ? 1 : dto.getNum();
            l.price = sg.getSeckillPrice();
            result.add(l);
        } else if ("buyNow".equals(dto.getBuyType())) {
            GoodsItem item = goodsService.getGoodsItem(dto.getGoodsItemId());
            if (item == null) throw new BusinessException(ResultCode.GOODS_NOT_EXIST);
            Line l = new Line();
            l.item = item;
            l.goods = goodsService.getById(item.getGoodsId());
            l.num = dto.getNum() == null ? 1 : dto.getNum();
            l.price = item.getPrice();
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
                l.price = item.getPrice();
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
        // 未支付才允许取消；恢复库存（秒杀单回补秒杀库存）
        List<OrderGoods> ogs = orderGoodsMapper.selectList(
                Wrappers.<OrderGoods>lambdaQuery().eq(OrderGoods::getOrderId, id));
        boolean seckill = order.getOrderType() != null && order.getOrderType() == 2;
        for (OrderGoods og : ogs) {
            if (seckill) {
                seckillService.rollbackByItemId(og.getGoodsItemId(), og.getNum());
            } else {
                goodsService.restoreStock(og.getGoodsItemId(), og.getNum());
                goodsService.minusSales(og.getGoodsId(), og.getNum());
            }
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

    /** 按订单号查询 */
    public Order getByOrderSn(String orderSn) {
        return this.lambdaQuery().eq(Order::getOrderSn, orderSn).one();
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
        // 收货后结算分销佣金
        distributionService.settle(orderId);
    }

    /** 后台分单发货 */
    public void deliver(Long orderId, Long expressId, String expressName, String expressNo) {
        Order order = this.getById(orderId);
        if (order == null) throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        if (order.getOrderStatus() != 1) throw new BusinessException("订单 " + order.getOrderSn() + " 非待发货状态");
        this.lambdaUpdate().eq(Order::getId, orderId)
                .set(Order::getOrderStatus, 2)
                .set(Order::getExpressId, expressId)
                .set(Order::getExpressName, expressName)
                .set(Order::getExpressNo, expressNo)
                .set(Order::getDeliveryTime, LocalDateTime.now())
                .update();
    }

    /** 批量发货（指定快递，单号缺失则自动生成 前缀+序号） */
    @Transactional(rollbackFor = Exception.class)
    public int batchDeliver(List<Long> orderIds, Long expressId, String expressName, String expressNo) {
        int done = 0;
        int i = 1;
        for (Long id : orderIds) {
            Order order = this.getById(id);
            if (order == null || order.getOrderStatus() != 1) continue;
            String no = (expressNo == null || expressNo.isBlank())
                    ? "BATCH" + System.currentTimeMillis() + String.format("%03d", i)
                    : expressNo;
            deliver(id, expressId, expressName, no);
            done++;
            i++;
        }
        return done;
    }

    /** 物流轨迹（模拟，按订单状态生成） */
    public List<Map<String, Object>> expressTraces(Long userId, Long orderId) {
        Order order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        List<Map<String, Object>> traces = new ArrayList<>();
        traces.add(Map.of("time", order.getCreateTime(), "desc", "买家已下单"));
        if (order.getPayTime() != null) traces.add(Map.of("time", order.getPayTime(), "desc", "订单支付成功"));
        if (order.getDeliveryTime() != null) {
            traces.add(Map.of("time", order.getDeliveryTime(), "desc", "商家已发货（" + order.getExpressName() + " " + order.getExpressNo() + "）"));
            traces.add(Map.of("time", order.getDeliveryTime().plusHours(6), "desc", "包裹已到达转运中心"));
            traces.add(Map.of("time", order.getDeliveryTime().plusHours(30), "desc", "运输中，预计1-3天送达"));
        }
        if (order.getReceiveTime() != null) {
            traces.add(Map.of("time", order.getReceiveTime(), "desc", "已签收，感谢您的惠顾"));
        } else if (order.getOrderStatus() == 2) {
            traces.add(Map.of("time", LocalDateTime.now(), "desc", "派送中，请保持电话畅通"));
        }
        return traces;
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
