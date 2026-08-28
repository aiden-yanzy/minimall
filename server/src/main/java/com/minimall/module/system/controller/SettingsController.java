package com.minimall.module.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import com.minimall.common.result.R;
import com.minimall.module.system.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 设置中心 + 商城基础设置(C端只读)
 */
@Tag(name = "设置中心")
@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @Operation(summary = "C端基础设置(公开)")
    @SaIgnore
    @GetMapping("/public")
    public R<Map<String, Object>> publicSite() {
        return R.ok(settingsService.read("site"));
    }

    @Operation(summary = "全部设置")
    @SaCheckLogin(type = "admin")
    @GetMapping("/admin/all")
    public R<Map<String, Map<String, Object>>> all() {
        return R.ok(settingsService.all());
    }

    @Operation(summary = "保存分组设置")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "system:setting", type = "admin")
    @PostMapping("/admin/{group}")
    public R<Void> save(@PathVariable String group, @RequestBody Map<String, Object> data) {
        settingsService.save(group, data);
        return R.ok("保存成功", null);
    }
}