package com.minimall.module.stat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.minimall.common.result.R;
import com.minimall.module.stat.model.vo.StatVO;
import com.minimall.module.stat.service.StatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 后台统计报表
 */
@Tag(name = "后台-统计报表")
@RestController
@RequestMapping("/admin/stat")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminStatController {

    private final StatService statService;

    @Operation(summary = "销售概览（汇总 + 每日趋势 + 漏斗）")
    @GetMapping("/overview")
    public R<StatVO.Overview> overview(@RequestParam(defaultValue = "30") int days) {
        return R.ok(statService.overview(days));
    }

    @Operation(summary = "销售明细（分页）")
    @GetMapping("/sale-detail")
    public R<StatVO.DetailPage> saleDetail(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return R.ok(statService.detail(page, size, status, start, end));
    }

    @Operation(summary = "商品销售排行")
    @GetMapping("/sale-rank")
    public R<List<StatVO.GoodsRank>> saleRank(@RequestParam(defaultValue = "30") int days,
                                              @RequestParam(defaultValue = "10") int limit) {
        return R.ok(statService.rank(days, limit));
    }
}
