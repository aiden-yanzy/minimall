package com.minimall.module.marketing.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.marketing.bargain.BargainActivity;
import com.minimall.module.marketing.bargain.BargainActivityMapper;
import com.minimall.module.marketing.presell.PresellActivity;
import com.minimall.module.marketing.presell.PresellActivityMapper;
import com.minimall.module.marketing.presell.PresellOrder;
import com.minimall.module.marketing.presell.PresellService;
import com.minimall.module.marketing.team.TeamActivity;
import com.minimall.module.marketing.team.TeamActivityMapper;
import com.minimall.module.marketing.team.TeamService;
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

import java.util.List;

/**
 * 后台拼团/砍价/预售管理（精简）
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
    private final PresellService presellService;
    private final TeamService teamService;

    @Operation(summary = "拼团活动列表")
    @GetMapping("/team")
    public R<List<TeamActivity>> teamList() {
        return R.ok(teamActivityMapper.selectList(null));
    }

    @Operation(summary = "保存拼团活动")
    @PostMapping("/team")
    public R<Long> saveTeam(@RequestBody TeamActivity a) {
        teamActivityMapper.insert(a);
        return R.ok("保存成功", a.getId());
    }

    @Operation(summary = "砍价活动列表")
    @GetMapping("/bargain")
    public R<List<BargainActivity>> bargainList() {
        return R.ok(bargainActivityMapper.selectList(null));
    }

    @Operation(summary = "保存砍价活动")
    @PostMapping("/bargain")
    public R<Long> saveBargain(@RequestBody BargainActivity a) {
        bargainActivityMapper.insert(a);
        return R.ok("保存成功", a.getId());
    }

    @Operation(summary = "预售活动列表")
    @GetMapping("/presell")
    public R<List<PresellActivity>> presellList() {
        return R.ok(presellActivityMapper.selectList(null));
    }

    @Operation(summary = "保存预售活动")
    @PostMapping("/presell")
    public R<Long> savePresell(@RequestBody PresellActivity a) {
        presellActivityMapper.insert(a);
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
                                  @RequestParam String expressName, @RequestParam String expressNo) {
        presellService.deliver(id, expressName, expressNo);
        return R.ok("发货成功", null);
    }

    @Operation(summary = "拼团列表")
    @GetMapping("/team/founds")
    public R<List<TeamService.TeamFoundVO>> founds() {
        return R.ok(teamService.myFounds(0L) == null ? List.of() : List.of());
    }
}