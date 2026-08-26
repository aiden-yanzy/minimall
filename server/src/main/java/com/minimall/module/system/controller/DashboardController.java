package com.minimall.module.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.system.service.WorkbenchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 后台工作台/数据报表
 */
@Tag(name = "后台-工作台")
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class DashboardController {

    private final WorkbenchService workbenchService;

    @Operation(summary = "统计数据")
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        return R.ok(workbenchService.stats());
    }
}