package com.minimall.module.marketing.team;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minimall.common.exception.BusinessException;
import com.minimall.common.result.ResultCode;
import com.minimall.module.goods.model.entity.Goods;
import com.minimall.module.goods.model.entity.GoodsItem;
import com.minimall.module.goods.service.GoodsService;
import com.minimall.module.order.mapper.OrderGoodsMapper;
import com.minimall.module.order.mapper.OrderMapper;
import com.minimall.module.order.model.entity.Order;
import com.minimall.module.order.model.entity.OrderGoods;
import com.minimall.module.user.service.UserService;
import com.minimall.module.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 拼团：下单(拼团价) → 支付 → 开团/参团 → 满员成团
 */
@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamActivityMapper activityMapper;
    private final TeamFoundMapper foundMapper;
    private final TeamJoinMapper joinMapper;
    private final GoodsService goodsService;
    private final OrderMapper orderMapper;
    private final OrderGoodsMapper orderGoodsMapper;
    private final UserService userService;
    private final WalletService walletService;

    /** 拼团超时时间：开团后 24 小时未成团则失败退款 */
    public static final java.time.Duration EXPIRE_AFTER = java.time.Duration.ofHours(24);

    /** 拼团活动列表（含商品信息） */
    public List<TeamActivityVO> list() {
        LocalDateTime now = LocalDateTime.now();
        return activityMapper.selectList(Wrappers.<TeamActivity>lambdaQuery()
                        .eq(TeamActivity::getStatus, 1)
                        .le(TeamActivity::getStartTime, now)
                        .ge(TeamActivity::getEndTime, now)
                        .orderByDesc(TeamActivity::getId))
                .stream().map(a -> new TeamActivityVO(a, goodsService)).toList();
    }

    /** 拼团下单（创建 type=3 订单，待支付） */
    @Transactional(rollbackFor = Exception.class)
    public Long buy(Long userId, TeamBuyDTO dto) {
        TeamActivity act = activityMapper.selectById(dto.getActivityId());
        if (act == null || act.getStatus() != 1) throw new BusinessException("拼团活动不存在");
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(act.getStartTime()) || now.isAfter(act.getEndTime())) {
            throw new BusinessException("拼团活动不在进行中");
        }
        int num = dto.getNum() == null ? 1 : dto.getNum();
        GoodsItem item = goodsService.getGoodsItem(act.getGoodsItemId());
        Goods goods = goodsService.getById(act.getGoodsId());
        if (item == null || item.getStock() < num) throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
        if (StrUtil.isBlank(dto.getContact()) || StrUtil.isBlank(dto.getMobile()) || StrUtil.isBlank(dto.getAddress())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "请填写完整收货信息");
        }
        long amount = act.getTeamPrice() * num;

        Order order = new Order();
        order.setOrderSn("TM" + IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setOrderType(3);
        order.setOrderStatus(0);
        order.setPayStatus(0);
        order.setPayType(dto.getPayType() == null ? 1 : dto.getPayType());
        order.setTotalAmount(amount);
        order.setPayAmount(amount);
        order.setContact(dto.getContact());
        order.setMobile(dto.getMobile());
        order.setAddress(dto.getAddress());
        order.setDeliveryType(1);
        orderMapper.insert(order);

        OrderGoods og = new OrderGoods();
        og.setOrderId(order.getId());
        og.setUserId(userId);
        og.setGoodsId(act.getGoodsId());
        og.setGoodsItemId(act.getGoodsItemId());
        og.setGoodsName(goods == null ? act.getName() : goods.getName());
        og.setGoodsImage(goods == null ? "" : goods.getImage());
        og.setSpecValueStr(item.getSpecValueStr());
        og.setPrice(act.getTeamPrice());
        og.setNum(num);
        og.setTotalPrice(amount);
        orderGoodsMapper.insert(og);

        if (!goodsService.deductStock(item.getId(), num)) {
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
        }
        goodsService.addSales(act.getGoodsId(), num);
        return order.getId();
    }

    /** 支付后开团/参团；满员自动成团 */
    @Transactional(rollbackFor = Exception.class)
    public TeamJoinVO join(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        if (order.getOrderType() != 3) throw new BusinessException("非拼团订单");
        if (order.getPayStatus() != 1) throw new BusinessException("请先支付");
        // 已参团？
        if (joinMapper.selectCount(Wrappers.<TeamJoin>lambdaQuery().eq(TeamJoin::getOrderId, orderId)) > 0) {
            throw new BusinessException("该订单已参团");
        }
        // 找进行中的团（未满员）
        List<TeamFound> founds = foundMapper.selectList(Wrappers.<TeamFound>lambdaQuery()
                .eq(TeamFound::getActivityId, order.getOrderType() == 3 ? activityOf(order) : 0)
                .eq(TeamFound::getStatus, 0)
                .orderByAsc(TeamFound::getId));
        // 需要 activityId：从订单商品倒查 activity
        TeamFound found = null;
        for (TeamFound f : founds) {
            if (joinCount(f.getId()) < teamNumOf(f.getActivityId())) { found = f; break; }
        }
        boolean isLeader = false;
        if (found == null) {
            Long activityId = activityIdOf(order);
            found = new TeamFound();
            found.setActivityId(activityId);
            found.setUserId(userId);
            found.setOrderId(orderId);
            found.setStatus(0);
            foundMapper.insert(found);
            isLeader = true;
        }
        TeamJoin join = new TeamJoin();
        join.setFoundId(found.getId());
        join.setActivityId(found.getActivityId());
        join.setUserId(userId);
        join.setOrderId(orderId);
        joinMapper.insert(join);

        int count = joinCount(found.getId());
        boolean success = false;
        if (activityMapper.selectById(found.getActivityId()) != null
                && count >= activityMapper.selectById(found.getActivityId()).getTeamNum()) {
            foundMapper.update(null, Wrappers.<TeamFound>lambdaUpdate()
                    .eq(TeamFound::getId, found.getId())
                    .set(TeamFound::getStatus, 1)
                    .set(TeamFound::getSuccessTime, LocalDateTime.now()));
            success = true;
        }
        TeamJoinVO vo = new TeamJoinVO();
        vo.setFoundId(found.getId());
        vo.setJoinedNum(count);
        vo.setSuccess(success);
        vo.setLeader(isLeader);
        return vo;
    }

    private int joinCount(Long foundId) {
        Long c = joinMapper.selectCount(Wrappers.<TeamJoin>lambdaQuery().eq(TeamJoin::getFoundId, foundId));
        return c == null ? 0 : c.intValue();
    }

    private int teamNumOf(Long activityId) {
        TeamActivity a = activityMapper.selectById(activityId);
        return a == null ? 2 : a.getTeamNum();
    }

    private Long activityIdOf(Order order) {
        // 通过订单商品反查：找含该 SKU 的进行中拼团活动
        List<OrderGoods> ogs = orderGoodsMapper.selectList(Wrappers.<OrderGoods>lambdaQuery().eq(OrderGoods::getOrderId, order.getId()));
        if (ogs.isEmpty()) throw new BusinessException("订单商品缺失");
        Long itemId = ogs.get(0).getGoodsItemId();
        TeamActivity a = activityMapper.selectOne(Wrappers.<TeamActivity>lambdaQuery()
                .eq(TeamActivity::getGoodsItemId, itemId).eq(TeamActivity::getStatus, 1).last("limit 1"));
        if (a == null) throw new BusinessException("拼团活动不存在");
        return a.getId();
    }

    private Long activityOf(Order order) {
        return activityIdOf(order);
    }

    /**
     * 扫描超时未成团的团：标记失败（status=2），团内已支付订单自动退款、恢复库存。
     * 条件：开团超过 24 小时仍为拼团中。
     */
    @Transactional(rollbackFor = Exception.class)
    public int scanExpired() {
        LocalDateTime deadline = LocalDateTime.now().minus(EXPIRE_AFTER);
        List<TeamFound> founds = foundMapper.selectList(Wrappers.<TeamFound>lambdaQuery()
                .eq(TeamFound::getStatus, 0)
                .lt(TeamFound::getCreateTime, deadline));
        int expired = 0;
        for (TeamFound found : founds) {
            foundMapper.update(null, Wrappers.<TeamFound>lambdaUpdate()
                    .eq(TeamFound::getId, found.getId())
                    .set(TeamFound::getStatus, 2));
            for (TeamJoin join : joinMapper.selectList(Wrappers.<TeamJoin>lambdaQuery()
                    .eq(TeamJoin::getFoundId, found.getId()))) {
                refundOrder(join.getOrderId(), join.getUserId());
            }
            expired++;
        }
        return expired;
    }

    /** 拼团失败退款：恢复库存 + 订单取消 + 余额退回/流水记录 */
    @Transactional(rollbackFor = Exception.class)
    private void refundOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getOrderStatus() != 1) return; // 仅待发货(已支付未发货)退款
        // 恢复库存
        for (OrderGoods og : orderGoodsMapper.selectList(Wrappers.<OrderGoods>lambdaQuery()
                .eq(OrderGoods::getOrderId, orderId))) {
            goodsService.restoreStock(og.getGoodsItemId(), og.getNum());
            goodsService.minusSales(og.getGoodsId(), og.getNum());
        }
        // 余额支付则真实退款，否则模拟退款（记录流水）
        if (order.getPayType() != null && order.getPayType() == 3
                && order.getPayAmount() != null && order.getPayAmount() > 0) {
            walletService.changeBalance(userId, order.getPayAmount(), 3, "拼团失败自动退款:" + order.getOrderSn());
        } else if (order.getPayAmount() != null && order.getPayAmount() > 0) {
            walletService.logOnly(userId, 3, order.getPayAmount(), "拼团失败自动退款(模拟):" + order.getOrderSn());
        }
        orderMapper.update(null, Wrappers.<Order>lambdaUpdate()
                .eq(Order::getId, orderId)
                .set(Order::getOrderStatus, 4)
                .set(Order::getCancelTime, LocalDateTime.now())
                .set(Order::getCancelReason, "拼团超时未成团，已自动退款"));
    }

    /** 我发起的/参与的团 */
    public List<TeamFoundVO> myFounds(Long userId) {
        List<TeamFound> founds = foundMapper.selectList(Wrappers.<TeamFound>lambdaQuery().eq(TeamFound::getUserId, userId));
        List<TeamJoin> joins = joinMapper.selectList(Wrappers.<TeamJoin>lambdaQuery().eq(TeamJoin::getUserId, userId));
        return founds.stream().map(f -> {
            TeamFoundVO vo = new TeamFoundVO();
            vo.setId(f.getId());
            vo.setActivityName(activityMapper.selectById(f.getActivityId()) == null ? "" : activityMapper.selectById(f.getActivityId()).getName());
            vo.setStatus(f.getStatus());
            vo.setJoinedNum(joinCount(f.getId()));
            vo.setTeamNum(teamNumOf(f.getActivityId()));
            vo.setRole("团长");
            return vo;
        }).toList();
    }

    // ---------- VO ----------
    @lombok.Data
    public static class TeamActivityVO {
        private Long id;
        private String name;
        private Long goodsId;
        private Long goodsItemId;
        private Long teamPrice;
        private Long marketPrice;
        private Integer teamNum;
        private Integer perLimit;
        private String goodsName;
        private String goodsImage;
        private String specValueStr;

        public TeamActivityVO(TeamActivity a, GoodsService goodsService) {
            this.id = a.getId();
            this.name = a.getName();
            this.goodsId = a.getGoodsId();
            this.goodsItemId = a.getGoodsItemId();
            this.teamPrice = a.getTeamPrice();
            this.teamNum = a.getTeamNum();
            this.perLimit = a.getPerLimit();
            Goods g = goodsService.getById(a.getGoodsId());
            GoodsItem it = goodsService.getGoodsItem(a.getGoodsItemId());
            if (g != null) { this.goodsName = g.getName(); this.goodsImage = g.getImage(); }
            if (it != null) { this.marketPrice = it.getPrice(); this.specValueStr = it.getSpecValueStr(); }
        }
    }

    @lombok.Data
    public static class TeamJoinVO {
        private Long foundId;
        private int joinedNum;
        private boolean success;
        private boolean leader;
    }

    @lombok.Data
    public static class TeamFoundVO {
        private Long id;
        private String activityName;
        private Integer status;
        private int joinedNum;
        private int teamNum;
        private String role;
    }
}