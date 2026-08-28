package com.minimall.module.freight.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.freight.model.entity.FreightConfig;
import com.minimall.module.freight.model.entity.GoodsDeliveryTemplate;
import com.minimall.module.freight.service.FreightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台运费模板管理
 */
@Tag(name = "后台-运费模板")
@RestController
@RequestMapping("/admin/freight")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminFreightController {

    private final FreightService freightService;

    @Data
    public static class TemplateSave {
        private GoodsDeliveryTemplate template;
        private List<FreightConfig> rules;
    }

    @Operation(summary = "模板列表")
    @GetMapping("/template")
    public R<List<GoodsDeliveryTemplate>> templates() {
        return R.ok(freightService.templates());
    }

    @Operation(summary = "模板规则")
    @GetMapping("/template/{id}/rules")
    public R<List<FreightConfig>> rules(@PathVariable Long id) {
        return R.ok(freightService.configs(id));
    }

    @Operation(summary = "保存模板(含规则)")
    @PostMapping("/template")
    public R<Long> save(@RequestBody TemplateSave body) {
        return R.ok("保存成功", freightService.saveTemplate(body.getTemplate(), body.getRules()));
    }

    @Operation(summary = "删除模板")
    @DeleteMapping("/template/{id}")
    public R<Void> remove(@PathVariable Long id) {
        freightService.removeTemplate(id);
        return R.ok();
    }
}