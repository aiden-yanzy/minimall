package com.minimall.module.marketing.bargain;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 砍价：发起 → 好友帮砍(每人一次,递减) → 砍到底价成交下单
 */
@Service
@RequiredArgsConstructor
public class BargainService {

    private final BargainActivityMapper activityMapper;
    private final BargainInitiateMapper initiateMapper;
    private final BargainHelpMapper helpMapper;
    private final GoodsService goodsService;
    private final OrderMapper orderMapper;
    private final OrderGoodsMapper orderGoodsMapper;

    /** 砍价活动列表 */
    public List<BargainActivityVO> list() {
        LocalDateTime now = LocalDateTime.now();
        return activityMapper.selectList(Wrappers.<BargainActivity>lambdaQuery()
                        .eq(BargainActivity::getStatus, 1)
                        .le(BargainActivity::getStartTime, now)
                        .ge(BargainActivity::getEndTime, now))
                .stream().map(a -> {
                    BargainActivityVO vo = new BargainActivityVO();
                    vo.setId(a.getId());
                    vo.setName(a.getName());
                    vo.setGoodsId(a.getGoodsId());
                    vo.setGoodsItemId(a.getGoodsItemId());
                    vo.setBasePrice(a.getBasePrice());
                    vo.setFloorPrice(a.getFloorPrice());
                    vo.setHelpNum(a.getHelpNum());
                    Goods g = goodsService.getById(a.getGoodsId());
                    GoodsItem it = goodsService.getGoodsItem(a.getGoodsItemId());
                    if (g != null) { vo.setGoodsName(g.getName()); vo.setGoodsImage(g.getImage()); }
                    if (it != null) vo.setSpecValueStr(it.getSpecValueStr());
                    return vo;
                }).toList();
    }

    /** 发起砍价 */
    @Transactional(rollbackFor = Exception.class)
    public Long start(Long userId, Long activityId) {
        BargainActivity act = activityMapper.selectById(activityId);
        if (act == null || act.getStatus() != 1) throw new BusinessException("砍价活动不存在");
        // 同一活动同一人最多 1 个进行中
        if (initiateMapper.selectCount(Wrappers.<BargainInitiate>lambdaQuery()
                .eq(BargainInitiate::getActivityId, activityId)
                .eq(BargainInitiate::getUserId, userId)
                .eq(BargainInitiate::getStatus, 0)) > 0) {
            throw new BusinessException("已有进行中的砍价");
        }
        BargainInitiate ini = new BargainInitiate();
        ini.setActivityId(activityId);
        ini.setUserId(userId);
        ini.setCurrentPrice(act.getBasePrice());
        ini.setHelpedCount(0);
        ini.setStatus(0);
        initiateMapper.insert(ini);
        return ini.getId();
    }

    /** 帮砍 */
    @Transactional(rollbackFor = Exception.class)
    public BargainHelpVO help(Long userId, Long initiateId) {
        BargainInitiate ini = initiateMapper.selectById(initiateId);
        if (ini == null || ini.getStatus() != 0) throw new BusinessException("砍价单不存在或已结束");
        if (ini.getUserId().equals(userId)) throw new BusinessException("不能帮自己砍");
        if (helpMapper.selectCount(Wrappers.<BargainHelp>lambdaQuery()
                .eq(BargainHelp::getInitiateId, initiateId)
                .eq(BargainHelp::getHelperUserId, userId)) > 0) {
            throw new BusinessException("你已经帮砍过了");
        }
        BargainActivity act = activityMapper.selectById(ini.getActivityId());
        if (act == null) throw new BusinessException("活动不存在");

        int remaining = Math.max(0, act.getHelpNum() - ini.getHelpedCount());
        if (remaining <= 0 || ini.getCurrentPrice() <= act.getFloorPrice()) {
            throw new BusinessException("已砍到底价");
        }
        // 把剩余可砍额度尽量均摊：每次 (当前价-底价) / 剩余次数，至少 1 分
        long reduce = Math.max(1, (ini.getCurrentPrice() - act.getFloorPrice()) / remaining);
        BargainHelp help = new BargainHelp();
        help.setInitiateId(initiateId);
        help.setHelperUserId(userId);
        help.setReduceAmount(reduce);
        helpMapper.insert(help);

        long newPrice = Math.max(act.getFloorPrice(), ini.getCurrentPrice() - reduce);
        int helped = ini.getHelpedCount() + 1;
        int status = (newPrice <= act.getFloorPrice() || helped >= act.getHelpNum()) ? 1 : 0;
        initiateMapper.update(null, Wrappers.<BargainInitiate>lambdaUpdate()
                .eq(BargainInitiate::getId, initiateId)
                .set(BargainInitiate::getCurrentPrice, newPrice)
                .set(BargainInitiate::getHelpedCount, helped)
                .set(BargainInitiate::getStatus, status));

        BargainHelpVO vo = new BargainHelpVO();
        vo.setReduce(reduce);
        vo.setCurrentPrice(newPrice);
        vo.setHelpedCount(helped);
        vo.setNeedHelp(act.getHelpNum());
        vo.setDone(status == 1);
        return vo;
    }

    /** 我的砍价列表 */
    public List<BargainInitiateVO> myList(Long userId) {
        return initiateMapper.selectList(Wrappers.<BargainInitiate>lambdaQuery()
                        .eq(BargainInitiate::getUserId, userId)
                        .orderByDesc(BargainInitiate::getId))
                .stream().map(ini -> {
                    BargainInitiateVO vo = new BargainInitiateVO();
                    BargainActivity act = activityMapper.selectById(ini.getActivityId());
                    vo.setId(ini.getId());
                    vo.setActivityId(ini.getActivityId());
                    vo.setActivityName(act == null ? "" : act.getName());
                    vo.setGoodsName(act == null ? "" : (goodsService.getById(act.getGoodsId()) == null ? "" : goodsService.getById(act.getGoodsId()).getName()));
                    vo.setGoodsImage(act == null ? "" : (goodsService.getById(act.getGoodsId()) == null ? "" : goodsService.getById(act.getGoodsId()).getImage()));
                    vo.setCurrentPrice(ini.getCurrentPrice());
                    vo.setBasePrice(act == null ? 0 : act.getBasePrice());
                    vo.setFloorPrice(act == null ? 0 : act.getFloorPrice());
                    vo.setHelpedCount(ini.getHelpedCount());
                    vo.setNeedHelp(act == null ? 0 : act.getHelpNum());
                    vo.setStatus(ini.getStatus());
                    vo.setOrderId(ini.getOrderId());
                    return vo;
                }).toList();
    }

    /** 砍成后下单 */
    @Transactional(rollbackFor = Exception.class)
    public Long order(Long userId, Long initiateId, String contact, String mobile, String address) {
        BargainInitiate ini = initiateMapper.selectById(initiateId);
        if (ini == null || !ini.getUserId().equals(userId)) throw new BusinessException("砍价单不存在");
        if (ini.getStatus() != 1) throw new BusinessException("砍价尚未成交");
        if (ini.getOrderId() != null) throw new BusinessException("已用该砍价单下过单");
        if (StrUtil.isBlank(contact) || StrUtil.isBlank(mobile) || StrUtil.isBlank(address)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "请填写完整收货信息");
        }
        BargainActivity act = activityMapper.selectById(ini.getActivityId());
        GoodsItem item = goodsService.getGoodsItem(act.getGoodsItemId());
        Goods goods = goodsService.getById(act.getGoodsId());
        if (item == null || item.getStock() < 1) throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);

        Order order = new Order();
        order.setOrderSn("BG" + IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setOrderType(5);
        order.setOrderStatus(0);
        order.setPayStatus(0);
        order.setPayType(1);
        order.setTotalAmount(ini.getCurrentPrice());
        order.setPayAmount(ini.getCurrentPrice());
        order.setContact(contact);
        order.setMobile(mobile);
        order.setAddress(address);
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
        og.setPrice(ini.getCurrentPrice());
        og.setNum(1);
        og.setTotalPrice(ini.getCurrentPrice());
        orderGoodsMapper.insert(og);
        goodsService.deductStock(item.getId(), 1);
        goodsService.addSales(act.getGoodsId(), 1);
        initiateMapper.update(null, Wrappers.<BargainInitiate>lambdaUpdate()
                .eq(BargainInitiate::getId, initiateId).set(BargainInitiate::getOrderId, order.getId()));
        return order.getId();
    }

    @lombok.Data
    public static class BargainActivityVO {
        private Long id;
        private String name;
        private Long goodsId;
        private Long goodsItemId;
        private Long basePrice;
        private Long floorPrice;
        private Integer helpNum;
        private String goodsName;
        private String goodsImage;
        private String specValueStr;
    }

    @lombok.Data
    public static class BargainHelpVO {
        private Long reduce;
        private Long currentPrice;
        private Integer helpedCount;
        private Integer needHelp;
        private boolean done;
    }

    @lombok.Data
    public static class BargainInitiateVO {
        private Long id;
        private Long activityId;
        private String activityName;
        private String goodsName;
        private String goodsImage;
        private Long currentPrice;
        private Long basePrice;
        private Long floorPrice;
        private Integer helpedCount;
        private Integer needHelp;
        private Integer status;
        private Long orderId;
    }
}