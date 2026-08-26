package com.minimall.module.marketing.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.marketing.model.entity.Coupon;
import com.minimall.module.marketing.model.entity.CouponList;
import com.minimall.module.marketing.service.CouponService;
import com.minimall.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端优惠券
 */
@Tag(name = "C端-优惠券")
@RestController
@RequestMapping("/shop/coupon")
@RequiredArgsConstructor
@SaCheckLogin(type = "user")
public class CouponController {

    private final CouponService couponService;
    private final UserService userService;

    @Operation(summary = "领券中心")
    @GetMapping("/center")
    public R<List<Coupon>> center() {
        return R.ok(couponService.claimList());
    }

    @Operation(summary = "领取优惠券")
    @PostMapping("/claim/{couponId}")
    public R<Void> claim(@PathVariable Long couponId) {
        couponService.claim(userService.getCurrentUserId(), couponId);
        return R.ok("领取成功", null);
    }

    @Operation(summary = "我的优惠券")
    @GetMapping("/mine")
    public R<List<CouponList>> mine(@RequestParam(required = false) Integer status) {
        return R.ok(couponService.myCoupons(userService.getCurrentUserId(), status));
    }
}