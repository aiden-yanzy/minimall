package com.minimall.module.marketing.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minimall.common.result.R;
import com.minimall.module.marketing.model.entity.Coupon;
import com.minimall.module.marketing.model.entity.FullReduction;
import com.minimall.module.marketing.service.CouponService;
import com.minimall.module.marketing.service.FullReductionService;
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

/**
 * 后台营销管理：优惠券 + 满减
 */
@Tag(name = "后台-营销管理")
@RestController
@RequestMapping("/admin/marketing")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminMarketingController {

    private final CouponService couponService;
    private final FullReductionService fullReductionService;

    // ---------------- 优惠券 ----------------

    @Operation(summary = "优惠券列表")
    @GetMapping("/coupon")
    public R<IPage<Coupon>> couponList(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "10") long size) {
        return R.ok(couponService.page(new Page<>(page, size), null));
    }

    @Operation(summary = "保存优惠券")
    @PostMapping("/coupon")
    public R<Long> saveCoupon(@RequestBody Coupon coupon) {
        coupon.setId(coupon.getId() == null ? null : coupon.getId());
        couponService.saveOrUpdate(coupon);
        return R.ok("保存成功", coupon.getId());
    }

    @Operation(summary = "启停优惠券")
    @PutMapping("/coupon/status/{id}")
    public R<Void> couponStatus(@PathVariable Long id, @RequestParam Integer status) {
        couponService.lambdaUpdate().eq(Coupon::getId, id).set(Coupon::getStatus, status).update();
        return R.ok();
    }

    @Operation(summary = "删除优惠券")
    @DeleteMapping("/coupon/{id}")
    public R<Void> removeCoupon(@PathVariable Long id) {
        couponService.removeById(id);
        return R.ok();
    }

    // ---------------- 满减 ----------------

    @Operation(summary = "满减规则列表")
    @GetMapping("/full-reduction")
    public R<java.util.List<FullReduction>> reductionList() {
        return R.ok(fullReductionService.list());
    }

    @Operation(summary = "保存满减规则")
    @PostMapping("/full-reduction")
    public R<Long> saveReduction(@RequestBody FullReduction fr) {
        fullReductionService.saveOrUpdate(fr);
        return R.ok("保存成功", fr.getId());
    }

    @Operation(summary = "删除满减规则")
    @DeleteMapping("/full-reduction/{id}")
    public R<Void> removeReduction(@PathVariable Long id) {
        fullReductionService.removeById(id);
        return R.ok();
    }
}