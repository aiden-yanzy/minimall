package com.minimall.module.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.goods.model.entity.Goods;
import com.minimall.module.user.model.entity.UserAddress;
import com.minimall.module.user.service.MemberService;
import com.minimall.module.user.service.UserService;
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
 * C 端会员中心：收货地址 / 收藏 / 足迹
 */
@Tag(name = "C端-会员中心")
@RestController
@RequestMapping("/shop/member")
@RequiredArgsConstructor
@SaCheckLogin(type = "user")
public class MemberController {

    private final MemberService memberService;
    private final UserService userService;
    private final com.minimall.module.user.service.UserLevelService levelService;

    // ---------- 地址 ----------
    @Operation(summary = "地址列表")
    @GetMapping("/address")
    public R<List<UserAddress>> addresses() {
        return R.ok(memberService.addresses(userService.getCurrentUserId()));
    }

    @Operation(summary = "默认地址")
    @GetMapping("/address/default")
    public R<UserAddress> defaultAddress() {
        return R.ok(memberService.defaultAddress(userService.getCurrentUserId()));
    }

    @Operation(summary = "保存地址(新增/编辑)")
    @PostMapping("/address")
    public R<Long> saveAddress(@RequestBody UserAddress a) {
        return R.ok("保存成功", memberService.saveAddress(userService.getCurrentUserId(), a));
    }

    @Operation(summary = "设为默认")
    @PutMapping("/address/{id}/default")
    public R<Void> setDefault(@PathVariable Long id) {
        memberService.setDefault(userService.getCurrentUserId(), id);
        return R.ok();
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/address/{id}")
    public R<Void> removeAddress(@PathVariable Long id) {
        memberService.removeAddress(userService.getCurrentUserId(), id);
        return R.ok();
    }

    // ---------- 收藏 ----------
    @Operation(summary = "收藏/取消收藏")
    @PostMapping("/collect/toggle")
    public R<Boolean> toggleCollect(@RequestParam Long goodsId) {
        return R.ok(memberService.toggleCollect(userService.getCurrentUserId(), goodsId));
    }

    @Operation(summary = "是否已收藏")
    @GetMapping("/collect/status")
    public R<Boolean> collectStatus(@RequestParam Long goodsId) {
        return R.ok(memberService.isCollected(userService.getCurrentUserId(), goodsId));
    }

    @Operation(summary = "我的收藏")
    @GetMapping("/collect/list")
    public R<List<Goods>> collectList() {
        return R.ok(memberService.collectList(userService.getCurrentUserId()));
    }

    // ---------- 足迹 ----------
    @Operation(summary = "记录浏览")
    @PostMapping("/footprint")
    public R<Void> recordFootprint(@RequestParam Long goodsId) {
        memberService.recordFootprint(userService.getCurrentUserId(), goodsId);
        return R.ok();
    }

    @Operation(summary = "我的足迹")
    @GetMapping("/footprint/list")
    public R<List<Goods>> footprintList() {
        return R.ok(memberService.footprintList(userService.getCurrentUserId()));
    }

    @Operation(summary = "清空足迹")
    @DeleteMapping("/footprint")
    public R<Void> clearFootprint() {
        memberService.clearFootprint(userService.getCurrentUserId());
        return R.ok();
    }

    // ---------- 会员等级 ----------

    @Operation(summary = "我的会员等级（含下一档进度）")
    @GetMapping("/level")
    public R<com.minimall.module.user.model.vo.MemberLevelVO> myLevel() {
        return R.ok(levelService.memberLevel(userService.getCurrentUserId()));
    }

    @Operation(summary = "等级权益一览")
    @GetMapping("/level/list")
    public R<java.util.List<com.minimall.module.user.model.entity.UserLevel>> levelList() {
        return R.ok(levelService.enabledLevels());
    }
}
