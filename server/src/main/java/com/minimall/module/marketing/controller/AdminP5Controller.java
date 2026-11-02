package com.minimall.module.marketing.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.goods.model.entity.Goods;
import com.minimall.module.goods.model.entity.GoodsItem;
import com.minimall.module.goods.service.GoodsService;
import com.minimall.module.marketing.bargain.BargainActivity;
import com.minimall.module.marketing.bargain.BargainActivityMapper;
import com.minimall.module.marketing.presell.PresellActivity;
import com.minimall.module.marketing.presell.PresellActivityMapper;
import com.minimall.module.marketing.presell.PresellOrder;
import com.minimall.module.marketing.presell.PresellService;
import com.minimall.module.marketing.team.TeamActivity;
import com.minimall.module.marketing.team.TeamActivityMapper;
import com.minimall.module.marketing.team.TeamFound;
import com.minimall.module.marketing.team.TeamFoundMapper;
import com.minimall.module.marketing.team.TeamService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minimall.module.user.model.entity.User;
import com.minimall.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 后台拼团/砍价/预售管理。
 *
 * <p>列表统一回填商品名与规格（原来只返回 goodsId，后台页面只能看到 "#1"）；
 * 保存统一 upsert（原来只 insert，编辑一次就多建一条活动）。
 */
@Tag(name = "后台-拼团/砍价/预售")
@RestController
@RequestMapping("/admin/p5")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminP5Controller {

    private final TeamActivityMapper teamActivityMapper;
    private final BargainActivityMapper bargainActivityMapper;
    private final PresellActivityMapper presellActivityMapper;
    private final TeamFoundMapper teamFoundMapper;
    private final PresellService presellService;
    private final TeamService teamService;
    private final GoodsService goodsService;
    private final UserService userService;

    // ---------- 拼团 ----------

    @Operation(summary = "拼团活动列表")
    @GetMapping("/team")
    public R<List<Map<String, Object>>> teamList() {
        List<TeamActivity> acts = teamActivityMapper.selectList(
                Wrappers.<TeamActivity>lambdaQuery().orderByDesc(TeamActivity::getId));
        return R.ok(acts.stream().map(this::teamVO).collect(Collectors.toList()));
    }

    @Operation(summary = "保存拼团活动（新增或更新）")
    @PostMapping("/team")
    public R<Long> saveTeam(@RequestBody TeamActivity a) {
        if (a.getId() == null) teamActivityMapper.insert(a);
        else teamActivityMapper.updateById(a);
        return R.ok("保存成功", a.getId());
    }

    private Map<String, Object> teamVO(TeamActivity a) {
        Map<String, Object> m = baseVO(a.getId(), a.getName(), a.getGoodsId(), a.getGoodsItemId());
        m.put("teamPrice", a.getTeamPrice());
        m.put("teamNum", a.getTeamNum());
        m.put("perLimit", a.getPerLimit());
        m.put("startTime", a.getStartTime());
        m.put("endTime", a.getEndTime());
        m.put("status", a.getStatus());
        return m;
    }

    // ---------- 砍价 ----------

    @Operation(summary = "砍价活动列表")
    @GetMapping("/bargain")
    public R<List<Map<String, Object>>> bargainList() {
        List<BargainActivity> acts = bargainActivityMapper.selectList(
                Wrappers.<BargainActivity>lambdaQuery().orderByDesc(BargainActivity::getId));
        List<Map<String, Object>> out = new ArrayList<>();
        for (BargainActivity a : acts) {
            Map<String, Object> m = baseVO(a.getId(), a.getName(), a.getGoodsId(), a.getGoodsItemId());
            m.put("basePrice", a.getBasePrice());
            m.put("floorPrice", a.getFloorPrice());
            m.put("helpNum", a.getHelpNum());
            m.put("startTime", a.getStartTime());
            m.put("endTime", a.getEndTime());
            m.put("status", a.getStatus());
            out.add(m);
        }
        return R.ok(out);
    }

    @Operation(summary = "保存砍价活动（新增或更新）")
    @PostMapping("/bargain")
    public R<Long> saveBargain(@RequestBody BargainActivity a) {
        if (a.getBasePrice() != null && a.getFloorPrice() != null && a.getFloorPrice() >= a.getBasePrice()) {
            return R.fail("底价必须低于原价");
        }
        if (a.getId() == null) bargainActivityMapper.insert(a);
        else bargainActivityMapper.updateById(a);
        return R.ok("保存成功", a.getId());
    }

    // ---------- 预售 ----------

    @Operation(summary = "预售活动列表")
    @GetMapping("/presell")
    public R<List<Map<String, Object>>> presellList() {
        List<PresellActivity> acts = presellActivityMapper.selectList(
                Wrappers.<PresellActivity>lambdaQuery().orderByDesc(PresellActivity::getId));
        List<Map<String, Object>> out = new ArrayList<>();
        for (PresellActivity a : acts) {
            Map<String, Object> m = baseVO(a.getId(), a.getName(), a.getGoodsId(), a.getGoodsItemId());
            m.put("deposit", a.getDeposit());
            m.put("finalPrice", a.getFinalPrice());
            m.put("stock", a.getStock());
            m.put("sales", a.getSales());
            m.put("startTime", a.getStartTime());
            m.put("endTime", a.getEndTime());
            m.put("finalEndTime", a.getFinalEndTime());
            m.put("status", a.getStatus());
            out.add(m);
        }
        return R.ok(out);
    }

    @Operation(summary = "保存预售活动（新增或更新）")
    @PostMapping("/presell")
    public R<Long> savePresell(@RequestBody PresellActivity a) {
        if (a.getId() == null) {
            if (a.getSales() == null) a.setSales(0);
            presellActivityMapper.insert(a);
        } else {
            presellActivityMapper.updateById(a);
        }
        return R.ok("保存成功", a.getId());
    }

    @Operation(summary = "预售订单列表")
    @GetMapping("/presell/orders")
    public R<List<PresellOrder>> presellOrders() {
        return R.ok(presellService.adminOrders());
    }

    @Operation(summary = "预售订单发货")
    @PostMapping("/presell/orders/{id}/deliver")
    public R<Void> deliverPresell(@PathVariable Long id,
                                  @RequestParam(required = false) String expressName,
                                  @RequestParam(required = false) String expressNo,
                                  @RequestBody(required = false) Map<String, Object> body) {
        String name = expressName;
        String no = expressNo;
        if (body != null) {
            if (name == null && body.get("expressName") != null) name = String.valueOf(body.get("expressName"));
            if (no == null && body.get("expressNo") != null) no = String.valueOf(body.get("expressNo"));
        }
        if (no == null || no.isBlank()) return R.fail("请填写快递单号");
        presellService.deliver(id, name == null ? "顺丰速运" : name, no);
        return R.ok("发货成功", null);
    }

    // ---------- 拼团记录 ----------

    @Operation(summary = "开团记录（全站）")
    @GetMapping("/team/founds")
    public R<List<Map<String, Object>>> founds() {
        List<TeamFound> list = teamFoundMapper.selectList(
                Wrappers.<TeamFound>lambdaQuery().orderByDesc(TeamFound::getId).last("limit 200"));
        if (list.isEmpty()) return R.ok(List.of());
        Map<Long, TeamActivity> acts = teamActivityMapper.selectList(null).stream()
                .collect(Collectors.toMap(TeamActivity::getId, a -> a, (a, b) -> a));
        List<Long> userIds = list.stream().map(TeamFound::getUserId).filter(java.util.Objects::nonNull).distinct().toList();
        Map<Long, User> users = userIds.isEmpty() ? Map.of()
                : userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        List<Map<String, Object>> out = new ArrayList<>();
        for (TeamFound f : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", f.getId());
            TeamActivity a = acts.get(f.getActivityId());
            m.put("activityName", a == null ? "-" : a.getName());
            m.put("needNum", a == null ? 0 : a.getTeamNum());
            m.put("joinNum", teamService.joinCount(f.getId()));
            m.put("status", f.getStatus());
            m.put("successTime", f.getSuccessTime());
            LocalDateTime base = f.getCreateTime() == null ? LocalDateTime.now() : f.getCreateTime();
            m.put("expireTime", base.plus(TeamService.EXPIRE_AFTER));
            User u = users.get(f.getUserId());
            m.put("leaderName", u == null ? "-" : u.getNickname());
            out.add(m);
        }
        return R.ok(out);
    }

    @Operation(summary = "手动扫描超时团并退款")
    @PostMapping("/team/scan")
    public R<Integer> scanTimeout() {
        int n = teamService.scanExpired();
        return R.ok("处理 " + n + " 个超时团", n);
    }

    // ---------- 公共 ----------

    private Map<String, Object> baseVO(Long id, String name, Long goodsId, Long goodsItemId) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("goodsId", goodsId);
        m.put("goodsItemId", goodsItemId);
        Goods g = goodsId == null ? null : goodsService.getById(goodsId);
        m.put("goodsName", g == null ? null : g.getName());
        m.put("goodsImage", g == null ? null : g.getImage());
        GoodsItem it = goodsItemId == null ? null : goodsService.getGoodsItem(goodsItemId);
        m.put("specValueStr", it == null ? null : it.getSpecValueStr());
        return m;
    }
}
