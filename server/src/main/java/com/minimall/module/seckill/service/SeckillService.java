package com.minimall.module.seckill.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minimall.common.exception.BusinessException;
import com.minimall.common.result.ResultCode;
import com.minimall.module.goods.model.entity.Goods;
import com.minimall.module.goods.model.entity.GoodsItem;
import com.minimall.module.goods.service.GoodsService;
import com.minimall.module.order.mapper.OrderGoodsMapper;
import com.minimall.module.order.mapper.OrderMapper;
import com.minimall.module.order.model.entity.Order;
import com.minimall.module.order.model.entity.OrderGoods;
import com.minimall.module.seckill.mapper.SeckillActivityMapper;
import com.minimall.module.seckill.mapper.SeckillGoodsMapper;
import com.minimall.module.seckill.model.entity.SeckillActivity;
import com.minimall.module.seckill.model.entity.SeckillGoods;
import com.minimall.module.seckill.model.vo.SeckillGoodsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeckillService extends ServiceImpl<SeckillGoodsMapper, SeckillGoods> {

    private static final String STOCK_KEY = "seckill:stock:";

    private final SeckillActivityMapper activityMapper;
    private final GoodsService goodsService;
    private final OrderMapper orderMapper;
    private final OrderGoodsMapper orderGoodsMapper;
    private final StringRedisTemplate redis;

    // ---------------- 场次 ----------------

    public List<SeckillActivity> activityList() {
        return activityMapper.selectList(Wrappers.<SeckillActivity>lambdaQuery()
                .orderByDesc(SeckillActivity::getId));
    }

    public void saveActivity(SeckillActivity a) {
        if (StrUtil.isBlank(a.getName())) throw new BusinessException("场次名称不能为空");
        if (a.getStartTime() == null || a.getEndTime() == null || !a.getEndTime().isAfter(a.getStartTime())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "请设置正确的开始/结束时间");
        }
        if (a.getId() == null) activityMapper.insert(a);
        else activityMapper.updateById(a);
    }

    public void updateActivityStatus(Long id, Integer status) {
        SeckillActivity act = activityMapper.selectById(id);
        if (act == null) throw new BusinessException("场次不存在");
        act.setStatus(status);
        activityMapper.updateById(act);
    }

    // ---------------- 秒杀商品（后台） ----------------

    public void saveGoods(SeckillGoods sg) {
        if (sg.getActivityId() == null || sg.getGoodsItemId() == null || sg.getSeckillPrice() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        GoodsItem item = goodsService.getGoodsItem(sg.getGoodsItemId());
        if (item == null) throw new BusinessException(ResultCode.GOODS_NOT_EXIST);
        if (sg.getSeckillPrice() >= item.getPrice()) {
            throw new BusinessException("秒杀价必须低于原价");
        }
        sg.setGoodsId(item.getGoodsId());
        if (sg.getId() == null) {
            sg.setSeckillSales(0);
            this.save(sg);
        } else {
            this.updateById(sg);
        }
        initRedisStock(sg.getId());
    }

    public void removeGoods(Long id) {
        this.removeById(id);
        redis.delete(STOCK_KEY + id);
    }

    // ---------------- C 端 ----------------

    /** 进行中 + 未开始的场次及商品 */
    public List<SeckillActivityVO> shopList() {
        LocalDateTime now = LocalDateTime.now();
        List<SeckillActivity> acts = activityMapper.selectList(Wrappers.<SeckillActivity>lambdaQuery()
                .eq(SeckillActivity::getStatus, 1)
                .and(q -> q.le(SeckillActivity::getStartTime, now).or(w -> w.gt(SeckillActivity::getStartTime, now)))
                .orderByAsc(SeckillActivity::getStartTime));
        return acts.stream().map(a -> {
            SeckillActivityVO vo = new SeckillActivityVO(a);
            // 抢购中 = 已开始 且 未结束（只判断开始会让过期场次显示"抢购中"，下单却报"秒杀已结束"）
            boolean started = a.getStartTime() != null && !a.getStartTime().isAfter(now);
            boolean notEnded = a.getEndTime() == null || a.getEndTime().isAfter(now);
            vo.setStarted(started && notEnded);
            vo.setGoodsList(this.listGoods(a.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    private List<SeckillGoodsVO> listGoods(Long activityId) {
        return this.lambdaQuery().eq(SeckillGoods::getActivityId, activityId)
                .orderByAsc(SeckillGoods::getSort)
                .list().stream().map(sg -> toVO(sg)).collect(Collectors.toList());
    }

    private SeckillGoodsVO toVO(SeckillGoods sg) {
        SeckillGoodsVO vo = new SeckillGoodsVO();
        vo.setId(sg.getId());
        vo.setGoodsId(sg.getGoodsId());
        vo.setGoodsItemId(sg.getGoodsItemId());
        vo.setSeckillPrice(sg.getSeckillPrice());
        vo.setSeckillStock(sg.getSeckillStock());
        vo.setSeckillSales(sg.getSeckillSales());
        vo.setPerLimit(sg.getPerLimit());
        Goods g = goodsService.getById(sg.getGoodsId());
        GoodsItem it = goodsService.getGoodsItem(sg.getGoodsItemId());
        if (g != null) {
            vo.setGoodsName(g.getName());
            vo.setGoodsImage(g.getImage());
        }
        if (it != null) {
            vo.setMarketPrice(it.getPrice());
            vo.setSpecValueStr(it.getSpecValueStr());
        }
        vo.setRemainStock(remaining(sg.getId(), sg));
        return vo;
    }

    // ---------------- Redis 库存 ----------------

    /** 初始化/刷新 Redis 库存（= 剩余可秒库存） */
    public void initRedisStock(Long sgId) {
        SeckillGoods sg = this.getById(sgId);
        if (sg == null) return;
        long remain = Math.max(0, sg.getSeckillStock() - sg.getSeckillSales());
        redis.opsForValue().set(STOCK_KEY + sgId, String.valueOf(remain));
    }

    /** 当前剩余秒杀库存（读 Redis，缺失则回源初始化） */
    public long remaining(Long sgId, SeckillGoods sg) {
        String v = redis.opsForValue().get(STOCK_KEY + sgId);
        if (v == null) {
            initRedisStock(sgId);
            v = redis.opsForValue().get(STOCK_KEY + sgId);
        }
        return v == null ? 0 : Long.parseLong(v);
    }

    /** 预扣 Redis 库存（原子），成功返回剩余 */
    public long preDeduct(Long sgId, int num) {
        String key = STOCK_KEY + sgId;
        // Redis 没有这把 key（新上秒杀商品 / Redis 重启 / key 过期）→ 按 DB 回源
        if (Boolean.FALSE.equals(redis.hasKey(key))) {
            initRedisStock(sgId);
        }
        Long remain = redis.opsForValue().decrement(key, num);
        if (remain != null && remain < 0) {
            redis.opsForValue().increment(key, num);
            // key 漂移（例如历史上被扣成 0 后 Key 仍存在）：回源校正一次再试，
            // 避免「DB 有 50 件，用户却永远抢不到」
            initRedisStock(sgId);
            remain = redis.opsForValue().decrement(key, num);
            if (remain != null && remain < 0) {
                redis.opsForValue().increment(key, num);
                throw new BusinessException("手慢了，秒杀商品已抢完");
            }
        }
        return remain == null ? 0 : remain;
    }

    /** 订单创建成功后落库：扣秒杀库存+加销量 */
    public void commitDeduct(Long sgId, int num) {
        this.update(Wrappers.<SeckillGoods>lambdaUpdate()
                .eq(SeckillGoods::getId, sgId)
                .setSql("seckill_stock = seckill_stock - " + num)
                .setSql("seckill_sales = seckill_sales + " + num));
    }

    /** 失败/取消/退款：回补 DB + Redis */
    public void rollback(Long sgId, int num) {
        this.update(Wrappers.<SeckillGoods>lambdaUpdate()
                .eq(SeckillGoods::getId, sgId)
                .setSql("seckill_stock = seckill_stock + " + num)
                .setSql("seckill_sales = seckill_sales - " + num));
        redis.opsForValue().increment(STOCK_KEY + sgId, num);
    }

    /** 按 SKU 回补（取消/退款时对秒杀订单用） */
    public void rollbackByItemId(Long goodsItemId, int num) {
        LocalDateTime now = LocalDateTime.now();
        List<SeckillGoods> list = this.lambdaQuery().eq(SeckillGoods::getGoodsItemId, goodsItemId).list();
        for (SeckillGoods sg : list) {
            SeckillActivity act = activityMapper.selectById(sg.getActivityId());
            if (act != null && act.getStatus() == 1
                    && !now.isBefore(act.getStartTime()) && !now.isAfter(act.getEndTime())) {
                rollback(sg.getId(), num);
            }
        }
    }

    // ---------------- 下单校验 ----------------

    /** 秒杀下单前校验：场次时间 + 限购 + Redis 预扣，返回秒杀商品 */
    public SeckillGoods validateForBuy(Long sgId, Long userId, int num) {
        SeckillGoods sg = this.getById(sgId);
        if (sg == null) throw new BusinessException("秒杀商品不存在");
        SeckillActivity act = activityMapper.selectById(sg.getActivityId());
        LocalDateTime now = LocalDateTime.now();
        if (act == null || act.getStatus() != 1) throw new BusinessException("秒杀活动未开启");
        if (now.isBefore(act.getStartTime())) throw new BusinessException("秒杀还没开始");
        if (now.isAfter(act.getEndTime())) throw new BusinessException("秒杀已结束");
        // 每人限购
        int limit = sg.getPerLimit() == null ? 1 : sg.getPerLimit();
        Long bought = orderGoodsMapper.selectList(Wrappers.<OrderGoods>lambdaQuery()
                        .eq(OrderGoods::getUserId, userId)
                        .eq(OrderGoods::getGoodsItemId, sg.getGoodsItemId()))
                .stream()
                .filter(og -> isPaidSeckillOrder(og.getOrderId()))
                .mapToLong(OrderGoods::getNum).sum();
        if (bought + num > limit) {
            throw new BusinessException("每人限购" + limit + "件");
        }
        preDeduct(sgId, num);
        return sg;
    }

    private boolean isPaidSeckillOrder(Long orderId) {
        Order o = orderMapper.selectById(orderId);
        return o != null && o.getOrderType() == 2 && o.getOrderStatus() > 0 && o.getOrderStatus() != 4;
    }

    // ---------------- VO 包装 ----------------

    @lombok.Data
    public static class SeckillActivityVO {
        private Long id;
        private String name;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean started;
        private List<SeckillGoodsVO> goodsList;

        public SeckillActivityVO(SeckillActivity a) {
            this.id = a.getId();
            this.name = a.getName();
            this.startTime = a.getStartTime();
            this.endTime = a.getEndTime();
        }
    }
}