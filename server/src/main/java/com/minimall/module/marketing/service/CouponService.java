package com.minimall.module.marketing.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minimall.common.exception.BusinessException;
import com.minimall.module.marketing.mapper.CouponListMapper;
import com.minimall.module.marketing.mapper.CouponMapper;
import com.minimall.module.marketing.model.entity.Coupon;
import com.minimall.module.marketing.model.entity.CouponList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券：领券/我的券/可用券/后台管理
 */
@Service
@RequiredArgsConstructor
public class CouponService extends ServiceImpl<CouponMapper, Coupon> {

    private final CouponListMapper couponListMapper;

    // ---------------- C 端 ----------------

    /** 领券中心列表（启用且在有效期内） */
    public List<Coupon> claimList() {
        LocalDateTime now = LocalDateTime.now();
        return this.lambdaQuery()
                .eq(Coupon::getStatus, 1)
                .and(q -> q
                        .eq(Coupon::getValidType, 2)
                        .or(w -> w.eq(Coupon::getValidType, 1)
                                .and(x -> x.le(Coupon::getStartTime, now).ge(Coupon::getEndTime, now))))
                .orderByDesc(Coupon::getId)
                .list();
    }

    /** 领取 */
    public void claim(Long userId, Long couponId) {
        Coupon coupon = this.getById(couponId);
        if (coupon == null || coupon.getStatus() != 1) {
            throw new BusinessException("优惠券不存在或已停用");
        }
        // 每人限领
        Long received = couponListMapper.selectCount(Wrappers.<CouponList>lambdaQuery()
                .eq(CouponList::getCouponId, couponId)
                .eq(CouponList::getUserId, userId));
        if (received != null && received >= coupon.getPerLimit()) {
            throw new BusinessException("已达每人限领数量");
        }
        // 库存
        if (coupon.getTotalCount() != null && coupon.getTotalCount() > 0
                && coupon.getSendCount() != null && coupon.getSendCount() >= coupon.getTotalCount()) {
            throw new BusinessException("优惠券已领完");
        }
        // 生成用户券
        CouponList cl = new CouponList();
        cl.setCouponId(couponId);
        cl.setUserId(userId);
        cl.setCouponName(coupon.getName());
        cl.setType(coupon.getType());
        cl.setScope(coupon.getScope());
        cl.setScopeIds(coupon.getScopeIds());
        cl.setThreshold(coupon.getThreshold());
        cl.setDiscountValue(coupon.getDiscountValue());
        cl.setStatus(0);
        LocalDateTime now = LocalDateTime.now();
        cl.setReceiveTime(now);
        cl.setExpireTime(coupon.getValidType() == 2 ? now.plusDays(coupon.getValidDays()) : coupon.getEndTime());
        couponListMapper.insert(cl);
        // 发放数 +1
        this.update(Wrappers.<Coupon>lambdaUpdate().eq(Coupon::getId, couponId).setSql("send_count = send_count + 1"));
    }

    /** 我的优惠券 */
    public List<CouponList> myCoupons(Long userId, Integer status) {
        List<CouponList> list = couponListMapper.selectList(Wrappers.<CouponList>lambdaQuery()
                .eq(CouponList::getUserId, userId)
                .eq(status != null, CouponList::getStatus, status)
                .orderByDesc(CouponList::getId));
        // 更新过期状态
        LocalDateTime now = LocalDateTime.now();
        for (CouponList c : list) {
            if (c.getStatus() == 0 && c.getExpireTime() != null && c.getExpireTime().isBefore(now)) {
                couponListMapper.update(null, Wrappers.<CouponList>lambdaUpdate()
                        .eq(CouponList::getId, c.getId()).set(CouponList::getStatus, 2));
                c.setStatus(2);
            }
        }
        return list;
    }

    /** 某笔金额下可用的券（含未过门槛、未过期、未使用） */
    public List<CouponList> usable(Long userId, long amount) {
        myCoupons(userId, 0); // 顺带刷新过期
        return couponListMapper.selectList(Wrappers.<CouponList>lambdaQuery()
                .eq(CouponList::getUserId, userId)
                .eq(CouponList::getStatus, 0)
                .le(CouponList::getThreshold, amount)
                .and(q -> q.isNull(CouponList::getExpireTime).or(w -> w.gt(CouponList::getExpireTime, LocalDateTime.now()))));
    }

    /** 校验券可用性（不修改状态），返回券行 */
    public CouponList validateCoupon(Long userId, Long couponListId, long payable) {
        CouponList cl = couponListMapper.selectById(couponListId);
        if (cl == null || !cl.getUserId().equals(userId) || cl.getStatus() != 0) {
            throw new BusinessException("优惠券不可用");
        }
        if (cl.getExpireTime() != null && cl.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("优惠券已过期");
        }
        if (cl.getThreshold() > payable) {
            throw new BusinessException("未达到优惠券使用门槛");
        }
        return cl;
    }

    /** 核销优惠券：返回优惠金额 */
    @Transactional(rollbackFor = Exception.class)
    public long useCoupon(Long userId, Long couponListId, Long orderId, long payable) {
        CouponList cl = couponListMapper.selectById(couponListId);
        if (cl == null || !cl.getUserId().equals(userId) || cl.getStatus() != 0) {
            throw new BusinessException("优惠券不可用");
        }
        if (cl.getExpireTime() != null && cl.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("优惠券已过期");
        }
        if (cl.getThreshold() > payable) {
            throw new BusinessException("未达到优惠券使用门槛");
        }
        long discount = calcDiscount(cl.getType(), cl.getDiscountValue(), payable);
        couponListMapper.update(null, Wrappers.<CouponList>lambdaUpdate()
                .eq(CouponList::getId, couponListId)
                .set(CouponList::getStatus, 1)
                .set(CouponList::getUseTime, LocalDateTime.now())
                .set(CouponList::getUseOrderId, orderId));
        return discount;
    }

    /** 计算优惠金额：满减券=value；折扣券=payable*(1-价值/1000) */
    public static long calcDiscount(int type, long value, long payable) {
        if (type == 2) {
            long d = payable * (1000 - value) / 1000;
            return Math.max(0, d);
        }
        return Math.min(value, payable);
    }

    /** 校验券是否可用于某商品 */
    public static boolean scopeMatch(int scope, String scopeIds, Long goodsId, Long categoryId) {
        if (scope == 1) return true;
        if (StrUtil.isBlank(scopeIds)) return false;
        for (String s : scopeIds.split(",")) {
            try {
                long v = Long.parseLong(s.trim());
                if (scope == 2 && v == categoryId) return true;
                if (scope == 3 && v == goodsId) return true;
            } catch (NumberFormatException ignored) {
            }
        }
        return false;
    }
}