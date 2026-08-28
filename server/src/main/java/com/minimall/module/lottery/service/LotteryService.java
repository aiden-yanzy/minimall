package com.minimall.module.lottery.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minimall.common.exception.BusinessException;
import com.minimall.module.lottery.mapper.LuckyDrawMapper;
import com.minimall.module.lottery.mapper.LuckyDrawPrizeMapper;
import com.minimall.module.lottery.mapper.LuckyDrawRecordMapper;
import com.minimall.module.lottery.mapper.RegisterAwardMapper;
import com.minimall.module.lottery.model.entity.LuckyDraw;
import com.minimall.module.lottery.model.entity.LuckyDrawPrize;
import com.minimall.module.lottery.model.entity.LuckyDrawRecord;
import com.minimall.module.lottery.model.entity.RegisterAward;
import com.minimall.module.marketing.mapper.CouponListMapper;
import com.minimall.module.marketing.model.entity.CouponList;
import com.minimall.module.user.model.entity.User;
import com.minimall.module.user.service.UserService;
import com.minimall.module.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 幸运抽奖 + 注册送
 */
@Service
@RequiredArgsConstructor
public class LotteryService {

    private final LuckyDrawMapper drawMapper;
    private final LuckyDrawPrizeMapper prizeMapper;
    private final LuckyDrawRecordMapper recordMapper;
    private final RegisterAwardMapper awardMapper;
    private final CouponListMapper couponListMapper;
    private final UserService userService;
    private final WalletService walletService;

    /** 进行中的抽奖场次（含奖品） */
    public List<LuckyDraw> draws() {
        return drawMapper.selectList(Wrappers.<LuckyDraw>lambdaQuery().eq(LuckyDraw::getStatus, 1));
    }

    public List<LuckyDrawPrize> prizes(Long drawId) {
        return prizeMapper.selectList(Wrappers.<LuckyDrawPrize>lambdaQuery()
                .eq(LuckyDrawPrize::getDrawId, drawId).eq(LuckyDrawPrize::getStatus, 1));
    }

    /** 抽一次：扣积分 → 权重随机 → 发奖 */
    @Transactional(rollbackFor = Exception.class)
    public LuckyDrawRecord draw(Long userId, Long drawId) {
        LuckyDraw draw = drawMapper.selectById(drawId);
        if (draw == null || draw.getStatus() != 1) throw new BusinessException("抽奖活动不存在");
        long cost = draw.getIntegralCost() == null ? 0 : draw.getIntegralCost();
        User user = userService.getById(userId);
        if (user == null || user.getIntegral() == null || user.getIntegral() < cost) {
            throw new BusinessException("积分不足，需要" + cost + "积分");
        }
        // 扣积分
        userService.update(Wrappers.<User>lambdaUpdate().eq(User::getId, userId).setSql("integral = integral - " + cost));

        List<LuckyDrawPrize> pool = prizes(drawId).stream()
                .filter(p -> p.getTotal() == null || p.getTotal() == 0 || p.getUsed() < p.getTotal())
                .toList();
        if (pool.isEmpty()) throw new BusinessException("奖池已空，稍后再试");
        // 权重随机（未中奖品 weight 大）
        int sum = pool.stream().mapToInt(p -> p.getWeight() == null ? 1 : p.getWeight()).sum();
        int r = ThreadLocalRandom.current().nextInt(sum);
        LuckyDrawPrize hit = pool.get(pool.size() - 1);
        int acc = 0;
        for (LuckyDrawPrize p : pool) {
            acc += p.getWeight() == null ? 1 : p.getWeight();
            if (r < acc) { hit = p; break; }
        }
        // 扣库存
        prizeMapper.update(null, Wrappers.<LuckyDrawPrize>lambdaUpdate()
                .eq(LuckyDrawPrize::getId, hit.getId()).setSql("used = used + 1"));

        // 发奖
        if (hit.getPrizeType() == 1) {
            userService.update(Wrappers.<User>lambdaUpdate().eq(User::getId, userId)
                    .setSql("integral = integral + " + hit.getPrizeValue()));
        } else if (hit.getPrizeType() == 2) {
            walletService.changeBalance(userId, hit.getPrizeValue(), 7, "抽奖中奖");
        } else if (hit.getPrizeType() == 3) {
            // 优惠券：直接发放一张券
            CouponList cl = new CouponList();
            cl.setCouponId(hit.getPrizeValue());
            cl.setUserId(userId);
            cl.setCouponName("抽奖奖品券");
            cl.setThreshold(0L);
            cl.setDiscountValue(hit.getPrizeValue() % 100 == 0 ? hit.getPrizeValue() : 0);
            cl.setStatus(0);
            cl.setReceiveTime(LocalDateTime.now());
            cl.setExpireTime(LocalDateTime.now().plusDays(30));
            couponListMapper.insert(cl);
        }

        LuckyDrawRecord rec = new LuckyDrawRecord();
        rec.setDrawId(drawId);
        rec.setUserId(userId);
        rec.setPrizeName(hit.getPrizeName());
        rec.setPrizeType(hit.getPrizeType());
        rec.setPrizeValue(hit.getPrizeValue());
        recordMapper.insert(rec);
        return rec;
    }

    public List<LuckyDrawRecord> records(Long userId) {
        return recordMapper.selectList(Wrappers.<LuckyDrawRecord>lambdaQuery()
                .eq(LuckyDrawRecord::getUserId, userId).orderByDesc(LuckyDrawRecord::getId).last("limit 20"));
    }

    public List<LuckyDrawRecord> allRecords() {
        return recordMapper.selectList(Wrappers.<LuckyDrawRecord>lambdaQuery().orderByDesc(LuckyDrawRecord::getId));
    }

    // ---------------- 注册送 ----------------

    public RegisterAward registerAwardCfg() {
        return awardMapper.selectOne(Wrappers.<RegisterAward>lambdaQuery()
                .eq(RegisterAward::getStatus, 1).last("limit 1"));
    }

    /** 注册成功后发放奖励 */
    @Transactional(rollbackFor = Exception.class)
    public void grantRegisterAward(Long userId) {
        RegisterAward award = registerAwardCfg();
        if (award == null) return;
        if (award.getAwardType() == 1) {
            userService.update(Wrappers.<User>lambdaUpdate().eq(User::getId, userId)
                    .setSql("integral = integral + " + (award.getAwardValue() == null ? 0 : award.getAwardValue())));
        } else if (award.getAwardType() == 2) {
            walletService.changeBalance(userId, award.getAwardValue() == null ? 0 : award.getAwardValue(), 7, "注册奖励");
        } else if (award.getAwardType() == 3 && award.getCouponId() != null) {
            CouponList cl = new CouponList();
            cl.setCouponId(award.getCouponId());
            cl.setUserId(userId);
            cl.setCouponName("注册礼券");
            cl.setThreshold(0L);
            cl.setDiscountValue(0L);
            cl.setStatus(0);
            cl.setReceiveTime(LocalDateTime.now());
            cl.setExpireTime(LocalDateTime.now().plusDays(30));
            couponListMapper.insert(cl);
        }
    }

    public void saveAward(RegisterAward award) {
        if (award.getId() == null) awardMapper.insert(award);
        else awardMapper.updateById(award);
    }

    public void saveDraw(LuckyDraw d) {
        if (d.getId() == null) drawMapper.insert(d);
        else drawMapper.updateById(d);
    }

    public void savePrize(LuckyDrawPrize p) {
        if (p.getId() == null) prizeMapper.insert(p);
        else prizeMapper.updateById(p);
    }
}