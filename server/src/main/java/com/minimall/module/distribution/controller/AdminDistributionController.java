package com.minimall.module.distribution.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minimall.common.result.R;
import com.minimall.module.distribution.mapper.DistributionLevelMapper;
import com.minimall.module.distribution.mapper.DistributionOrderGoodsMapper;
import com.minimall.module.distribution.model.entity.DistributionLevel;
import com.minimall.module.distribution.model.entity.DistributionOrderGoods;
import com.minimall.module.distribution.service.DistributionService;
import com.minimall.module.order.model.entity.Order;
import com.minimall.module.order.service.OrderService;
import com.minimall.module.user.model.entity.User;
import com.minimall.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 后台分销管理：等级配置（费率/门槛）+ 分销员看板 + 佣金流水。
 *
 * <p>之前只有 C 端接口，后台要调费率或升级门槛只能改数据库。
 */
@Tag(name = "后台-分销")
@RestController
@RequestMapping("/admin/distribution")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminDistributionController {

    private final DistributionLevelMapper levelMapper;
    private final DistributionOrderGoodsMapper dogMapper;
    private final DistributionService distributionService;
    private final UserService userService;
    private final OrderService orderService;

    // ---------- 等级配置 ----------

    @Operation(summary = "分销等级列表")
    @GetMapping("/level")
    public R<List<DistributionLevel>> levels() {
        return R.ok(levelMapper.selectList(Wrappers.<DistributionLevel>lambdaQuery()
                .orderByAsc(DistributionLevel::getLevel)));
    }

    @Operation(summary = "保存分销等级")
    @SaCheckPermission(value = "distribution:manage", type = "admin")
    @PostMapping("/level")
    public R<Long> saveLevel(@RequestBody DistributionLevel level) {
        if (StrUtil.isBlank(level.getName())) return R.fail("等级名称不能为空");
        if (level.getCommissionRate() == null || level.getCommissionRate() < 0) return R.fail("佣金费率不能为空");
        if (level.getUpgradeAmount() == null) level.setUpgradeAmount(0L);
        if (level.getStatus() == null) level.setStatus(1);
        if (level.getLevel() == null) level.setLevel(1);
        if (level.getId() == null) levelMapper.insert(level);
        else levelMapper.updateById(level);
        return R.ok("保存成功", level.getId());
    }

    @Operation(summary = "删除分销等级")
    @SaCheckPermission(value = "distribution:manage", type = "admin")
    @DeleteMapping("/level/{id}")
    public R<Void> removeLevel(@PathVariable Long id) {
        levelMapper.deleteById(id);
        return R.ok();
    }

    // ---------- 分销员看板 ----------

    @Operation(summary = "分销员列表（按累计佣金倒序）")
    @GetMapping("/members")
    public R<List<Map<String, Object>>> members(@RequestParam(required = false) String keyword) {
        // 有下级或有过佣金的人都算分销员
        List<DistributionOrderGoods> all = dogMapper.selectList(Wrappers.<DistributionOrderGoods>lambdaQuery());
        Map<Long, Long> totalBySeller = all.stream()
                .filter(d -> d.getSellerUserId() != null)
                .collect(Collectors.groupingBy(DistributionOrderGoods::getSellerUserId,
                        Collectors.summingLong(d -> d.getCommission() == null ? 0L : d.getCommission())));
        if (totalBySeller.isEmpty()) return R.ok(List.of());

        List<User> sellers = userService.list(Wrappers.<User>lambdaQuery()
                .in(User::getId, totalBySeller.keySet())
                .and(StrUtil.isNotBlank(keyword), q -> q.like(User::getMobile, keyword).or().like(User::getNickname, keyword)));

        List<DistributionLevel> levels = levelMapper.selectList(Wrappers.<DistributionLevel>lambdaQuery()
                .eq(DistributionLevel::getStatus, 1).orderByAsc(DistributionLevel::getLevel));

        List<Map<String, Object>> out = new ArrayList<>();
        for (User u : sellers) {
            long total = totalBySeller.getOrDefault(u.getId(), 0L);
            DistributionLevel lv = distributionService.levelOf(total, levels);
            Map<String, Object> row = new HashMap<>();
            row.put("id", u.getId());
            row.put("nickname", u.getNickname());
            row.put("mobile", maskMobile(u.getMobile()));
            row.put("balance", u.getBalance());
            row.put("createTime", u.getCreateTime());
            row.put("totalCommission", total);
            row.put("teamCount", userService.count(Wrappers.<User>lambdaQuery().eq(User::getReferrerId, u.getId())));
            row.put("inviteCode", distributionService.inviteCode(u.getId()));
            row.put("levelName", lv == null ? "普通分销员" : lv.getName());
            row.put("rate", lv == null ? 0 : lv.getCommissionRate());
            out.add(row);
        }
        out.sort((a, b) -> Long.compare((Long) b.get("totalCommission"), (Long) a.get("totalCommission")));
        return R.ok(out);
    }

    // ---------- 佣金流水 ----------

    @Operation(summary = "佣金流水（带订单号与买卖双方）")
    @GetMapping("/orders")
    public R<List<Map<String, Object>>> orders(@RequestParam(required = false) Long sellerId) {
        List<DistributionOrderGoods> list = dogMapper.selectList(Wrappers.<DistributionOrderGoods>lambdaQuery()
                .eq(sellerId != null, DistributionOrderGoods::getSellerUserId, sellerId)
                .orderByDesc(DistributionOrderGoods::getId)
                .last("limit 200"));
        if (list.isEmpty()) return R.ok(List.of());

        List<Long> orderIds = list.stream().map(DistributionOrderGoods::getOrderId).distinct().toList();
        Map<Long, Order> orders = orderService.listByIds(orderIds).stream()
                .collect(Collectors.toMap(Order::getId, o -> o, (a, b) -> a));
        List<Long> userIds = new ArrayList<>();
        list.forEach(d -> {
            if (d.getSellerUserId() != null) userIds.add(d.getSellerUserId());
            if (d.getBuyerUserId() != null) userIds.add(d.getBuyerUserId());
        });
        Map<Long, User> users = userIds.isEmpty() ? Map.of()
                : userService.listByIds(userIds.stream().distinct().toList()).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        List<Map<String, Object>> out = new ArrayList<>();
        for (DistributionOrderGoods d : list) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", d.getId());
            row.put("orderId", d.getOrderId());
            Order o = orders.get(d.getOrderId());
            row.put("orderSn", o == null ? null : o.getOrderSn());
            row.put("goodsAmount", d.getGoodsAmount());
            row.put("rate", d.getRate());
            row.put("commission", d.getCommission());
            row.put("status", d.getStatus());
            row.put("createTime", d.getCreateTime());
            User seller = users.get(d.getSellerUserId());
            User buyer = users.get(d.getBuyerUserId());
            row.put("sellerName", seller == null ? "-" : seller.getNickname());
            row.put("buyerName", buyer == null ? "-" : buyer.getNickname());
            out.add(row);
        }
        return R.ok(out);
    }

    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 11) return mobile;
        return mobile.substring(0, 3) + "****" + mobile.substring(7);
    }
}
