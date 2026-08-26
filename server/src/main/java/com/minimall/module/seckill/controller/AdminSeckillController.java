package com.minimall.module.seckill.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.seckill.model.entity.SeckillActivity;
import com.minimall.module.seckill.model.entity.SeckillGoods;
import com.minimall.module.seckill.service.SeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台秒杀管理
 */
@Tag(name = "后台-秒杀管理")
@RestController
@RequestMapping("/admin/seckill")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminSeckillController {

    private final SeckillService seckillService;

    @Operation(summary = "场次列表")
    @GetMapping("/activity")
    public R<List<SeckillActivity>> activityList() {
        return R.ok(seckillService.activityList());
    }

    @Operation(summary = "保存场次")
    @PostMapping("/activity")
    public R<Long> saveActivity(@RequestBody SeckillActivity a) {
        seckillService.saveActivity(a);
        return R.ok("保存成功", a.getId());
    }

    @Operation(summary = "启停场次")
    @PutMapping("/activity/status/{id}")
    public R<Void> activityStatus(@PathVariable Long id, @RequestParam Integer status) {
        seckillService.updateActivityStatus(id, status);
        return R.ok();
    }

    @Operation(summary = "秒杀商品列表")
    @GetMapping("/goods")
    public R<List<SeckillGoods>> goodsList(@RequestParam(required = false) Long activityId) {
        return R.ok(seckillService.lambdaQuery()
                .eq(activityId != null, SeckillGoods::getActivityId, activityId)
                .list());
    }

    @Operation(summary = "保存秒杀商品")
    @PostMapping("/goods")
    public R<Long> saveGoods(@RequestBody SeckillGoods sg) {
        seckillService.saveGoods(sg);
        return R.ok("保存成功", sg.getId());
    }

    @Operation(summary = "删除秒杀商品")
    @DeleteMapping("/goods/{id}")
    public R<Void> removeGoods(@PathVariable Long id) {
        seckillService.removeGoods(id);
        return R.ok();
    }
}