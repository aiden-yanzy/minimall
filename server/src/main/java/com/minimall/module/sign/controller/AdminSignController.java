package com.minimall.module.sign.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.sign.model.entity.SignDaily;
import com.minimall.module.sign.service.SignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台签到配置
 */
@Tag(name = "后台-签到配置")
@RestController
@RequestMapping("/admin/sign")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminSignController {

    private final SignService signService;

    @Operation(summary = "签到配置")
    @GetMapping("/config")
    public R<SignDaily> config() {
        return R.ok(signService.config());
    }

    @Operation(summary = "保存签到配置")
    @PostMapping("/config")
    public R<Void> save(@RequestBody SignDaily d) {
        // 只有一条配置，更新或插入
        if (d.getId() != null) signService.updateById(d);
        else signService.save(d);
        return R.ok();
    }
}