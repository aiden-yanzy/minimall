package com.minimall.module.distribution.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minimall.common.exception.BusinessException;
import com.minimall.module.distribution.mapper.DistributionLevelMapper;
import com.minimall.module.distribution.mapper.DistributionOrderGoodsMapper;
import com.minimall.module.distribution.model.entity.DistributionLevel;
import com.minimall.module.distribution.model.entity.DistributionOrderGoods;
import com.minimall.module.order.mapper.OrderGoodsMapper;
import com.minimall.module.order.model.entity.OrderGoods;
import com.minimall.module.user.model.entity.User;
import com.minimall.module.user.service.UserService;
import com.minimall.module.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分销：绑定推荐关系（含邀请码）+ 收货后按卖家等级费率结算一级佣金。
 *
 * <p>费率口径：费率以「千分比」存储，100 = 10%。等级按**卖家本人累计佣金**匹配
 * distribution_level.upgrade_amount（0 = 无门槛），取达标的最高档。
 */
@Service
@RequiredArgsConstructor
public class DistributionService {

    private final DistributionLevelMapper levelMapper;
    private final DistributionOrderGoodsMapper dogMapper;
    private final OrderGoodsMapper orderGoodsMapper;
    private final UserService userService;
    private final WalletService walletService;

    // ---------- 邀请码 ----------

    /** 邀请码混淆参数：直接 base36(userId) 会得到 C/D/E 这种可枚举短码 */
    private static final long CODE_SALT = 193_939L;
    private static final long CODE_OFFSET = 1_000_003L;

    /** 用户 ID → 邀请码（混淆后 36 进制，不可枚举） */
    public String inviteCode(Long userId) {
        if (userId == null) return null;
        return Long.toString(userId * CODE_SALT + CODE_OFFSET, 36).toUpperCase();
    }

    /** 邀请码 → 用户 ID，非法返回 null */
    public Long decodeInviteCode(String code) {
        if (code == null || code.isBlank()) return null;
        try {
            long raw = Long.parseLong(code.trim().toUpperCase(), 36);
            long id = (raw - CODE_OFFSET) / CODE_SALT;
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ---------- 绑定 ----------

    /** 绑定推荐人（只能一次，不可绑定自己） */
    public void bind(Long userId, Long referrerId) {
        if (referrerId == null || referrerId.equals(userId)) throw new BusinessException("推荐人无效");
        User user = userService.getById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (user.getReferrerId() != null) throw new BusinessException("已绑定推荐人，不能修改");
        if (userService.getById(referrerId) == null) throw new BusinessException("推荐人不存在");
        userService.update(Wrappers.<User>lambdaUpdate().eq(User::getId, userId).set(User::getReferrerId, referrerId));
    }

    /** 用邀请码绑定 */
    public void bindByCode(Long userId, String code) {
        Long referrerId = decodeInviteCode(code);
        if (referrerId == null) throw new BusinessException("邀请码无效");
        bind(userId, referrerId);
    }

    /** 我的推荐人 */
    public User referrer(Long userId) {
        User me = userService.getById(userId);
        if (me == null || me.getReferrerId() == null) return null;
        return userService.getById(me.getReferrerId());
    }

    // ---------- 佣金 ----------

    /** 我的佣金汇总 */
    public long myCommission(Long userId) {
        return listSettled(userId).stream()
                .mapToLong(d -> d.getCommission() == null ? 0L : d.getCommission())
                .sum();
    }

    public List<DistributionOrderGoods> listSettled(Long userId) {
        return dogMapper.selectList(Wrappers.<DistributionOrderGoods>lambdaQuery()
                .eq(DistributionOrderGoods::getSellerUserId, userId)
                .orderByDesc(DistributionOrderGoods::getId));
    }

    /** 启用中的等级（按档位升序） */
    private List<DistributionLevel> levels() {
        return levelMapper.selectList(Wrappers.<DistributionLevel>lambdaQuery()
                .eq(DistributionLevel::getStatus, 1)
                .orderByAsc(DistributionLevel::getLevel));
    }

    /** 按累计佣金匹配卖家等级 */
    public DistributionLevel levelOf(long totalCommission, List<DistributionLevel> levels) {
        DistributionLevel matched = null;
        for (DistributionLevel l : levels) {
            long need = l.getUpgradeAmount() == null ? 0L : l.getUpgradeAmount();
            if (totalCommission >= need) {
                if (matched == null) {
                    matched = l;
                } else {
                    long cur = matched.getUpgradeAmount() == null ? 0L : matched.getUpgradeAmount();
                    if (need >= cur) matched = l;
                }
            }
        }
        return matched;
    }

    /** 我的分销等级信息（当前等级 / 费率 / 下一档 / 进度） */
    public Map<String, Object> myLevel(Long userId) {
        Map<String, Object> m = new HashMap<>();
        long total = myCommission(userId);
        List<DistributionLevel> all = levels();
        DistributionLevel cur = levelOf(total, all);
        if (cur == null) {
            m.put("levelName", "普通分销员");
            m.put("rate", 100);
            return m;
        }
        m.put("levelId", cur.getId());
        m.put("levelName", cur.getName());
        m.put("rate", cur.getCommissionRate());
        m.put("totalCommission", total);

        long curNeed = cur.getUpgradeAmount() == null ? 0L : cur.getUpgradeAmount();
        DistributionLevel next = null;
        List<DistributionLevel> sorted = new ArrayList<>(all);
        sorted.sort(Comparator.comparing(l -> l.getUpgradeAmount() == null ? 0L : l.getUpgradeAmount()));
        for (DistributionLevel l : sorted) {
            long need = l.getUpgradeAmount() == null ? 0L : l.getUpgradeAmount();
            if (need > curNeed) { next = l; break; }
        }
        if (next != null) {
            long nextNeed = next.getUpgradeAmount() == null ? 0L : next.getUpgradeAmount();
            m.put("nextLevelName", next.getName());
            m.put("nextRate", next.getCommissionRate());
            m.put("nextRequire", nextNeed);
            m.put("gap", Math.max(0L, nextNeed - total));
            m.put("percent", nextNeed <= curNeed ? 100 : (int) Math.min(100, (total - curNeed) * 100 / (nextNeed - curNeed)));
        } else {
            m.put("percent", 100);
            m.put("gap", 0L);
        }
        return m;
    }

    /** 分销中心概览 */
    public Map<String, Object> overview(Long userId) {
        Map<String, Object> m = new HashMap<>(myLevel(userId));
        List<DistributionOrderGoods> list = listSettled(userId);
        long total = list.stream().mapToLong(d -> d.getCommission() == null ? 0L : d.getCommission()).sum();
        LocalDate today = LocalDate.now();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        long monthSum = list.stream()
                .filter(d -> d.getCreateTime() != null && !d.getCreateTime().isBefore(monthStart))
                .mapToLong(d -> d.getCommission() == null ? 0L : d.getCommission()).sum();
        long todaySum = list.stream()
                .filter(d -> d.getCreateTime() != null && d.getCreateTime().toLocalDate().equals(today))
                .mapToLong(d -> d.getCommission() == null ? 0L : d.getCommission()).sum();

        long teamCount = userService.count(Wrappers.<User>lambdaQuery().eq(User::getReferrerId, userId));
        User me = userService.getById(userId);

        m.put("totalCommission", total);
        m.put("monthCommission", monthSum);
        m.put("todayCommission", todaySum);
        m.put("settledCount", list.size());
        m.put("teamCount", teamCount);
        m.put("balance", me == null ? 0L : me.getBalance());
        m.put("inviteCode", inviteCode(userId));
        m.put("rate", myLevel(userId).get("rate"));
        return m;
    }

    /** 我的团队（下级 + 各自贡献佣金） */
    public List<Map<String, Object>> team(Long userId) {
        List<User> members = userService.list(Wrappers.<User>lambdaQuery()
                .eq(User::getReferrerId, userId)
                .orderByDesc(User::getId));
        if (members.isEmpty()) return List.of();
        List<Long> ids = members.stream().map(User::getId).toList();
        Map<Long, Long> contrib = new HashMap<>();
        for (DistributionOrderGoods d : dogMapper.selectList(Wrappers.<DistributionOrderGoods>lambdaQuery()
                .eq(DistributionOrderGoods::getSellerUserId, userId))) {
            if (d.getBuyerUserId() == null || !ids.contains(d.getBuyerUserId())) continue;
            contrib.merge(d.getBuyerUserId(), d.getCommission() == null ? 0L : d.getCommission(), Long::sum);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (User u : members) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", u.getId());
            row.put("nickname", u.getNickname());
            row.put("avatar", u.getAvatar());
            row.put("mobile", u.getMobile() == null ? "" : u.getMobile().substring(0, 3) + "****" + u.getMobile().substring(7));
            row.put("createTime", u.getCreateTime());
            row.put("commission", contrib.getOrDefault(u.getId(), 0L));
            out.add(row);
        }
        return out;
    }

    /** 确认收货后结算佣金（一级：按**卖家等级**费率，费率千分比） */
    @Transactional(rollbackFor = Exception.class)
    public void settle(Long orderId) {
        List<OrderGoods> ogs = orderGoodsMapper.selectList(Wrappers.<OrderGoods>lambdaQuery().eq(OrderGoods::getOrderId, orderId));
        if (ogs.isEmpty()) return;
        User buyer = userService.getById(ogs.get(0).getUserId());
        if (buyer == null || buyer.getReferrerId() == null) return;

        Long sellerId = buyer.getReferrerId();
        List<DistributionLevel> all = levels();
        // 卖家费率按「卖家已结算佣金」匹配，而不是永远用第一档
        DistributionLevel sellerLevel = levelOf(myCommission(sellerId), all);
        int rate = sellerLevel == null || sellerLevel.getCommissionRate() == null ? 0 : sellerLevel.getCommissionRate();
        if (rate <= 0) return;

        for (OrderGoods og : ogs) {
            if (og.getIsAfterSale() != null && og.getIsAfterSale() == 1) continue;
            if (dogMapper.selectCount(Wrappers.<DistributionOrderGoods>lambdaQuery()
                    .eq(DistributionOrderGoods::getOrderGoodsId, og.getId())) > 0) continue;
            long amount = og.getTotalPrice() == null ? 0L : og.getTotalPrice();
            long commission = amount * rate / 1000;
            if (commission <= 0) continue;
            DistributionOrderGoods dog = new DistributionOrderGoods();
            dog.setOrderId(orderId);
            dog.setOrderGoodsId(og.getId());
            dog.setBuyerUserId(buyer.getId());
            dog.setSellerUserId(sellerId);
            dog.setGoodsAmount(amount);
            dog.setRate(rate);
            dog.setCommission(commission);
            dog.setStatus(1);
            dogMapper.insert(dog);
            walletService.changeBalance(sellerId, commission, 5, "分销佣金：订单" + orderId);
        }
    }
}
