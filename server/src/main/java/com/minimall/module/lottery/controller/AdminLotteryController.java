package com.minimall.module.lottery.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.lottery.model.entity.LuckyDraw;
import com.minimall.module.lottery.model.entity.LuckyDrawPrize;
import com.minimall.module.lottery.model.entity.LuckyDrawRecord;
import com.minimall.module.lottery.model.entity.RegisterAward;
import com.minimall.module.lottery.service.LotteryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台抽奖/注册送管理
 */
@Tag(name = "后台-抽奖/注册送")
@RestController
@RequestMapping("/admin/lottery")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminLotteryController {

    private final LotteryService lotteryService;

    @Operation(summary = "抽奖场次")
    @GetMapping("/draws")
    public R<List<LuckyDraw>> draws() {
        return R.ok(lotteryService.draws());
    }

    @Operation(summary = "保存场次")
    @PostMapping("/draws")
    public R<Long> saveDraw(@RequestBody LuckyDraw d) {
        lotteryService.saveDraw(d);
        return R.ok("保存成功", d.getId());
    }

    @Operation(summary = "奖品列表")
    @GetMapping("/prizes")
    public R<List<LuckyDrawPrize>> prizes(Long drawId) {
        return R.ok(drawId != null ? lotteryService.prizes(drawId) : lotteryService.prizes(1L));
    }

    @Operation(summary = "保存奖品")
    @PostMapping("/prizes")
    public R<Long> savePrize(@RequestBody LuckyDrawPrize p) {
        lotteryService.savePrize(p);
        return R.ok("保存成功", p.getId());
    }

    @Operation(summary = "中奖记录")
    @GetMapping("/records")
    public R<List<LuckyDrawRecord>> records() {
        return R.ok(lotteryService.allRecords());
    }

    @Operation(summary = "注册送配置")
    @GetMapping("/register-award")
    public R<RegisterAward> registerAward() {
        return R.ok(lotteryService.registerAwardCfg());
    }

    @Operation(summary = "保存注册送配置")
    @PostMapping("/register-award")
    public R<Void> saveRegisterAward(@RequestBody RegisterAward a) {
        lotteryService.saveAward(a);
        return R.ok("保存成功", null);
    }
}