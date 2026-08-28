package com.minimall.module.lottery.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.minimall.common.result.R;
import com.minimall.module.lottery.model.entity.LuckyDraw;
import com.minimall.module.lottery.model.entity.LuckyDrawPrize;
import com.minimall.module.lottery.model.entity.LuckyDrawRecord;
import com.minimall.module.lottery.service.LotteryService;
import com.minimall.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端幸运抽奖
 */
@Tag(name = "C端-幸运抽奖")
@RestController
@RequestMapping("/shop/lucky")
@RequiredArgsConstructor
public class LotteryController {

    private final LotteryService lotteryService;
    private final UserService userService;

    @Operation(summary = "抽奖场次")
    @SaIgnore
    @GetMapping("/draws")
    public R<List<LuckyDraw>> draws() {
        return R.ok(lotteryService.draws());
    }

    @Operation(summary = "场次奖品")
    @SaIgnore
    @GetMapping("/draws/{drawId}/prizes")
    public R<List<LuckyDrawPrize>> prizes(@PathVariable Long drawId) {
        return R.ok(lotteryService.prizes(drawId));
    }

    @Operation(summary = "抽一次")
    @SaCheckLogin(type = "user")
    @PostMapping("/draw/{drawId}")
    public R<LuckyDrawRecord> draw(@PathVariable Long drawId) {
        LuckyDrawRecord rec = lotteryService.draw(userService.getCurrentUserId(), drawId);
        String suffix = rec.getPrizeType() == 4 ? "" : " 获得 " + rec.getPrizeName();
        return R.ok("抽奖成功" + suffix, rec);
    }

    @Operation(summary = "我的抽奖记录")
    @SaCheckLogin(type = "user")
    @GetMapping("/records")
    public R<List<LuckyDrawRecord>> records() {
        return R.ok(lotteryService.records(userService.getCurrentUserId()));
    }
}