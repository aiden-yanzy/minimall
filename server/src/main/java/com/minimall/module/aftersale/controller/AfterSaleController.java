package com.minimall.module.aftersale.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.aftersale.model.dto.AfterSaleCreateDTO;
import com.minimall.module.aftersale.model.entity.AfterSale;
import com.minimall.module.aftersale.service.AfterSaleService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端售后
 */
@Tag(name = "C端-售后")
@RestController
@RequestMapping("/shop/after")
@RequiredArgsConstructor
@SaCheckLogin(type = "user")
public class AfterSaleController {

    private final AfterSaleService afterSaleService;
    private final UserService userService;

    @Operation(summary = "申请售后")
    @PostMapping("/create")
    public R<Long> create(@Valid @RequestBody AfterSaleCreateDTO dto) {
        return R.ok("申请成功", afterSaleService.create(userService.getCurrentUserId(), dto));
    }

    @Operation(summary = "我的售后列表")
    @GetMapping
    public R<List<com.minimall.module.aftersale.model.vo.AfterSaleVO>> myList() {
        return R.ok(afterSaleService.myList(userService.getCurrentUserId()));
    }

    @Operation(summary = "取消售后")
    @PostMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        afterSaleService.cancel(userService.getCurrentUserId(), id);
        return R.ok();
    }
}
