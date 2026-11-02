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
import com.minimall.module.order.mapper.OrderTraceMapper;
import com.minimall.module.order.model.dto.OrderCreateDTO;
import com.minimall.module.order.model.entity.Order;
import com.minimall.module.order.model.entity.OrderGoods;
import com.minimall.module.order.model.entity.OrderTrace;
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
    private final OrderTraceMapper orderTraceMapper;
    private final GoodsService goodsService;
    private final CartService cartService;
    private final CouponService couponService;
    private final FullReductionService fullReductionService;
    private final UserLevelService userLevelService;
    private final com.minimall.module.user.mapper.UserMapper userMapper;
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
                // 原子扣减：并发下抢不到就直接失败（前面的预检是 check-then-act，挡不住并发）
                if (!goodsService.deductStock(l.item.getId(), l.num)) {
                    throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
                }
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
        doCancel(order, reason);
    }

    /**
     * 系统自动取消（超时未支付）：无 userId 校验，但用 CAS 抢占状态，
     * 避免和「用户刚支付」并发时把已支付订单误取消。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean autoCancel(Long orderId, String reason) {
        Order order = this.getById(orderId);
        if (order == null || order.getOrderStatus() != 0) return false;
        boolean ok = this.lambdaUpdate()
                .eq(Order::getId, orderId)
                .eq(Order::getOrderStatus, 0)
                .set(Order::getOrderStatus, 4)
                .set(Order::getPayStatus, 0)
                .set(Order::getCancelTime, LocalDateTime.now())
                .set(Order::getCancelReason, reason)
                .update();
        if (!ok) return false;
        restoreForCancel(order);
        return true;
    }

    /** 取消收尾：恢复库存（秒杀单回补秒杀库存）+ 退回优惠券 */
    private void doCancel(Order order, String reason) {
        this.lambdaUpdate().eq(Order::getId, order.getId())
                .set(Order::getOrderStatus, 4)
                .set(Order::getCancelTime, LocalDateTime.now())
                .set(Order::getCancelReason, reason)
                .update();
        restoreForCancel(order);
    }

    private void restoreForCancel(Order order) {
        List<OrderGoods> ogs = orderGoodsMapper.selectList(
                Wrappers.<OrderGoods>lambdaQuery().eq(OrderGoods::getOrderId, order.getId()));
        boolean seckill = order.getOrderType() != null && order.getOrderType() == 2;
        for (OrderGoods og : ogs) {
            if (seckill) {
                seckillService.rollbackByItemId(og.getGoodsItemId(), og.getNum());
            } else {
                goodsService.restoreStock(og.getGoodsItemId(), og.getNum());
                goodsService.minusSales(og.getGoodsId(), og.getNum());
            }
        }
        // 券退回（否则订单取消了券还占着）
        try {
            couponService.releaseByOrder(order.getId());
        } catch (Exception ignore) {
        }
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

        // 支付成功才累加成长值（累计实付金额），再按成长值重算会员等级
        long paid = order.getPayAmount() == null ? 0L : order.getPayAmount();
        if (order.getUserId() != null) {
            userMapper.update(null, com.baomidou.mybatisplus.core.toolkit.Wrappers.<User>lambdaUpdate()
                    .eq(User::getId, order.getUserId())
                    .setSql("total_consume = total_consume + " + paid)
                    .setSql("total_order_num = total_order_num + 1"));
            userLevelService.refreshLevel(order.getUserId());
        }
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
        addTrace(orderId, "商家已发货（" + (expressName == null ? "快递" : expressName)
                + " " + (expressNo == null ? "-" : expressNo) + "）", "system");
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

    /** 物流轨迹（读 order_trace；历史订单没有落库数据时按订单状态兜底生成） */
    public List<Map<String, Object>> expressTraces(Long userId, Long orderId) {
        Order order = this.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        return tracesOf(order);
    }

    /** 后台查物流轨迹 */
    public List<Map<String, Object>> adminTraces(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        return tracesOf(order);
    }

    private List<Map<String, Object>> tracesOf(Order order) {
        List<OrderTrace> rows = orderTraceMapper.selectList(Wrappers.<OrderTrace>lambdaQuery()
                .eq(OrderTrace::getOrderId, order.getId())
                .orderByAsc(OrderTrace::getTraceTime)
                .orderByAsc(OrderTrace::getId));
        List<Map<String, Object>> list = new ArrayList<>();
        if (!rows.isEmpty()) {
            for (OrderTrace t : rows) {
                list.add(Map.of("time", t.getTraceTime(), "desc", t.getDescription()));
            }
            return list;
        }
        // 兜底：老订单没有轨迹记录，按既有时间点生成（只读，不再伪造未来节点）
        list.add(Map.of("time", order.getCreateTime(), "desc", "买家已下单"));
        if (order.getPayTime() != null) list.add(Map.of("time", order.getPayTime(), "desc", "订单支付成功"));
        if (order.getDeliveryTime() != null) {
            list.add(Map.of("time", order.getDeliveryTime(), "desc", "商家已发货（"
                    + (order.getExpressName() == null ? "快递" : order.getExpressName())
                    + " " + (order.getExpressNo() == null ? "-" : order.getExpressNo()) + "）"));
        }
        if (order.getReceiveTime() != null) list.add(Map.of("time", order.getReceiveTime(), "desc", "已签收，感谢您的惠顾"));
        return list;
    }

    /** 追加一条物流轨迹 */
    public void addTrace(Long orderId, String description, String operator) {
        addTraceAt(orderId, description, operator, LocalDateTime.now());
    }

    public void addTraceAt(Long orderId, String description, String operator, LocalDateTime time) {
        // 首条轨迹落库前，先把订单已有的历史节点补进来，避免时间线只剩新增的那一条
        Long existing = orderTraceMapper.selectCount(Wrappers.<OrderTrace>lambdaQuery().eq(OrderTrace::getOrderId, orderId));
        if (existing == null || existing == 0) {
            Order o = this.getById(orderId);
            if (o != null) {
                if (o.getCreateTime() != null) insertTrace(orderId, "买家已下单", "system", o.getCreateTime());
                if (o.getPayTime() != null) insertTrace(orderId, "订单支付成功", "system", o.getPayTime());
                if (o.getDeliveryTime() != null) {
                    insertTrace(orderId, "商家已发货（" + (o.getExpressName() == null ? "快递" : o.getExpressName())
                            + " " + (o.getExpressNo() == null ? "-" : o.getExpressNo()) + "）", "system", o.getDeliveryTime());
                }
                if (o.getReceiveTime() != null) insertTrace(orderId, "已签收，感谢您的惠顾", "system", o.getReceiveTime());
            }
        }
        insertTrace(orderId, description, operator, time);
    }

    private void insertTrace(Long orderId, String description, String operator, LocalDateTime time) {
        OrderTrace t = new OrderTrace();
        t.setOrderId(orderId);
        t.setDescription(description);
        t.setOperator(operator == null ? "system" : operator);
        t.setTraceTime(time == null ? LocalDateTime.now() : time);
        orderTraceMapper.insert(t);
    }

    /** demo 用：按快递节点顺序推进一条轨迹（后台点一下就走一步） */
    private static final String[] TRACE_STEPS = {
            "包裹已揽收", "已到达【始发转运中心】", "运输中，已发往【目的地转运中心】",
            "已到达【目的地转运中心】", "派送中，请保持电话畅通"
    };

    @Transactional(rollbackFor = Exception.class)
    public String advanceTrace(Long orderId, String operator) {
        Order order = this.getById(orderId);
        if (order == null) throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        if (order.getDeliveryTime() == null) throw new BusinessException("订单未发货，无法推进物流");
        List<OrderTrace> rows = orderTraceMapper.selectList(Wrappers.<OrderTrace>lambdaQuery()
                .eq(OrderTrace::getOrderId, orderId));
        int idx = Math.min(rows.size(), TRACE_STEPS.length - 1);
        // 已发过 N 条（含发货那条）→ 取下一条，最多到派送中
        int step = Math.min(Math.max(rows.size() - 1, 0), TRACE_STEPS.length - 1);
        String desc = TRACE_STEPS[step];
        addTrace(orderId, desc, operator);
        return desc;
    }

    /** 后台改价（仅待付款订单；差额记入 discountAmount，保留原价可追溯） */
    @Transactional(rollbackFor = Exception.class)
    public void changePrice(Long orderId, Long payAmount, String reason, String operator) {
        Order order = this.getById(orderId);
        if (order == null) throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        if (order.getOrderStatus() != 0) throw new BusinessException("只有待付款订单可以改价");
        if (payAmount == null || payAmount < 0) throw new BusinessException("改价金额不合法");
        if (reason == null || reason.isBlank()) throw new BusinessException("改价必须填写原因");
        long old = order.getPayAmount() == null ? 0L : order.getPayAmount();
        long baseDiscount = order.getDiscountAmount() == null ? 0L : order.getDiscountAmount();
        // 改价差额并入 discountAmount（负数为加价），payAmount 落到新值
        long newDiscount = baseDiscount + (old - payAmount);
        this.lambdaUpdate().eq(Order::getId, orderId)
                .set(Order::getPayAmount, payAmount)
                .set(Order::getDiscountAmount, newDiscount)
                .update();
        addTrace(orderId, "商家改价：" + (old / 100.0) + " 元 → " + (payAmount / 100.0)
                + " 元（原因：" + reason + "）", operator);
    }

    /** 订单商品行（供售后使用） */
    public List<OrderGoods> listOrderGoods(Long orderId) {
        return orderGoodsMapper.selectList(Wrappers.<OrderGoods>lambdaQuery().eq(OrderGoods::getOrderId, orderId));
    }

    /** 按 ID 批量取订单商品（售后列表回填用） */
    public List<OrderGoods> listOrderGoodsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return orderGoodsMapper.selectBatchIds(ids);
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
