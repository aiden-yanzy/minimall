package com.minimall.module.marketing.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.minimall.common.result.R;
import com.minimall.module.marketing.bargain.BargainService;
import com.minimall.module.marketing.presell.PresellBuyDTO;
import com.minimall.module.marketing.presell.PresellOrder;
import com.minimall.module.marketing.presell.PresellService;
import com.minimall.module.marketing.team.TeamBuyDTO;
import com.minimall.module.marketing.team.TeamService;
import com.minimall.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * C 端拼团 / 砍价 / 预售
 */
@Tag(name = "C端-拼团/砍价/预售")
@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class P5Controller {

    private final TeamService teamService;
    private final BargainService bargainService;
    private final PresellService presellService;
    private final UserService userService;

    // ---------- 拼团 ----------
    @Operation(summary = "拼团活动列表")
    @SaIgnore
    @GetMapping("/team/list")
    public R<List<TeamService.TeamActivityVO>> teamList() {
        return R.ok(teamService.list());
    }

    @Operation(summary = "拼团下单")
    @SaCheckLogin(type = "user")
    @PostMapping("/team/buy")
    public R<Long> teamBuy(@Valid @RequestBody TeamBuyDTO dto) {
        return R.ok("下单成功，请支付后参团", teamService.buy(userService.getCurrentUserId(), dto));
    }

    @Operation(summary = "开团/参团")
    @SaCheckLogin(type = "user")
    @PostMapping("/team/join/{orderId}")
    public R<TeamService.TeamJoinVO> teamJoin(@PathVariable Long orderId) {
        return R.ok(teamService.join(userService.getCurrentUserId(), orderId));
    }

    @Operation(summary = "我的拼团")
    @SaCheckLogin(type = "user")
    @GetMapping("/team/mine")
    public R<List<TeamService.TeamFoundVO>> teamMine() {
        return R.ok(teamService.myFounds(userService.getCurrentUserId()));
    }

    // ---------- 砍价 ----------
    @Operation(summary = "砍价活动列表")
    @SaIgnore
    @GetMapping("/bargain/list")
    public R<List<BargainService.BargainActivityVO>> bargainList() {
        return R.ok(bargainService.list());
    }

    @Operation(summary = "发起砍价")
    @SaCheckLogin(type = "user")
    @PostMapping("/bargain/start/{activityId}")
    public R<Long> bargainStart(@PathVariable Long activityId) {
        return R.ok("发起成功", bargainService.start(userService.getCurrentUserId(), activityId));
    }

    @Operation(summary = "帮砍")
    @SaCheckLogin(type = "user")
    @PostMapping("/bargain/help/{initiateId}")
    public R<BargainService.BargainHelpVO> bargainHelp(@PathVariable Long initiateId) {
        return R.ok(bargainService.help(userService.getCurrentUserId(), initiateId));
    }

    @Operation(summary = "我的砍价")
    @SaCheckLogin(type = "user")
    @GetMapping("/bargain/mine")
    public R<List<BargainService.BargainInitiateVO>> bargainMine() {
        return R.ok(bargainService.myList(userService.getCurrentUserId()));
    }

    @Operation(summary = "砍成下单")
    @SaCheckLogin(type = "user")
    @PostMapping("/bargain/order/{initiateId}")
    public R<Long> bargainOrder(@PathVariable Long initiateId,
                                @RequestParam String contact, @RequestParam String mobile, @RequestParam String address) {
        return R.ok("下单成功", bargainService.order(userService.getCurrentUserId(), initiateId, contact, mobile, address));
    }

    // ---------- 预售 ----------
    @Operation(summary = "预售活动列表")
    @SaIgnore
    @GetMapping("/presell/list")
    public R<List<com.minimall.module.marketing.presell.PresellActivity>> presellList() {
        return R.ok(presellService.list());
    }

    @Operation(summary = "付定金下单")
    @SaCheckLogin(type = "user")
    @PostMapping("/presell/order")
    public R<Long> presellOrder(@Valid @RequestBody PresellBuyDTO dto) {
        return R.ok("请支付定金", presellService.order(userService.getCurrentUserId(), dto));
    }

    @Operation(summary = "支付定金(模拟)")
    @SaCheckLogin(type = "user")
    @PostMapping("/presell/pay-deposit/{orderId}")
    public R<Void> presellDeposit(@PathVariable Long orderId) {
        presellService.payDeposit(userService.getCurrentUserId(), orderId);
        return R.ok("定金支付成功", null);
    }

    @Operation(summary = "支付尾款(模拟)")
    @SaCheckLogin(type = "user")
    @PostMapping("/presell/pay-final/{orderId}")
    public R<Void> presellFinal(@PathVariable Long orderId) {
        presellService.payFinal(userService.getCurrentUserId(), orderId);
        return R.ok("尾款支付成功", null);
    }

    @Operation(summary = "我的预售订单")
    @SaCheckLogin(type = "user")
    @GetMapping("/presell/mine")
    public R<List<PresellOrder>> presellMine() {
        return R.ok(presellService.myOrders(userService.getCurrentUserId()));
    }
}