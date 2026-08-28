package com.minimall.module.freeshipping.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.freeshipping.model.entity.FreeShipping;
import com.minimall.module.freeshipping.service.FreeShippingService;
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
 * 后台满额免邮管理
 */
@Tag(name = "后台-满额免邮")
@RestController
@RequestMapping("/admin/free-shipping")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminFreeShippingController {

    private final FreeShippingService freeShippingService;

    @Operation(summary = "规则列表")
    @GetMapping
    public R<List<FreeShipping>> rules() {
        return R.ok(freeShippingService.rules());
    }

    @Operation(summary = "保存规则")
    @PostMapping
    public R<Long> save(@RequestBody FreeShipping fs) {
        freeShippingService.saveOrUpdate(fs);
        return R.ok("保存成功", fs.getId());
    }

    @Operation(summary = "删除规则")
    @PostMapping("/remove/{id}")
    public R<Void> remove(Long id) {
        freeShippingService.removeById(id);
        return R.ok();
    }
}