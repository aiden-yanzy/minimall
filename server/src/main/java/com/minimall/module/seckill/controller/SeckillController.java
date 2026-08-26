package com.minimall.module.seckill.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.minimall.common.result.R;
import com.minimall.module.seckill.service.SeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端秒杀
 */
@Tag(name = "C端-秒杀")
@RestController
@RequestMapping("/shop/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    @Operation(summary = "秒杀场次与商品")
    @SaIgnore
    @GetMapping("/list")
    public R<List<SeckillService.SeckillActivityVO>> list() {
        return R.ok(seckillService.shopList());
    }
}