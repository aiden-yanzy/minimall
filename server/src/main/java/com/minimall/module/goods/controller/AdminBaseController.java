package com.minimall.module.goods.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minimall.common.result.R;
import com.minimall.module.goods.mapper.GoodsBrandMapper;
import com.minimall.module.goods.mapper.GoodsUnitMapper;
import com.minimall.module.goods.model.entity.GoodsBrand;
import com.minimall.module.goods.model.entity.GoodsUnit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台基础资料：品牌 / 单位
 */
@Tag(name = "后台-品牌/单位")
@RestController
@RequestMapping("/admin/base")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminBaseController {

    private final GoodsBrandMapper brandMapper;
    private final GoodsUnitMapper unitMapper;

    // ---------- 品牌 ----------
    @Operation(summary = "品牌列表")
    @GetMapping("/brand")
    public R<IPage<GoodsBrand>> brands(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "20") long size) {
        return R.ok(brandMapper.selectPage(new Page<>(page, size),
                Wrappers.<GoodsBrand>lambdaQuery().orderByAsc(GoodsBrand::getSort)));
    }

    @Operation(summary = "全部品牌(下拉用)")
    @GetMapping("/brand/all")
    public R<List<GoodsBrand>> brandsAll() {
        return R.ok(brandMapper.selectList(Wrappers.<GoodsBrand>lambdaQuery().eq(GoodsBrand::getIsShow, 1)));
    }

    @Operation(summary = "保存品牌")
    @PostMapping("/brand")
    public R<Long> saveBrand(@RequestBody GoodsBrand b) {
        if (b.getId() == null) brandMapper.insert(b);
        else brandMapper.updateById(b);
        return R.ok("保存成功", b.getId());
    }

    @Operation(summary = "删除品牌")
    @DeleteMapping("/brand/{id}")
    public R<Void> removeBrand(@PathVariable Long id) {
        brandMapper.deleteById(id);
        return R.ok();
    }

    // ---------- 单位 ----------
    @Operation(summary = "单位列表")
    @GetMapping("/unit")
    public R<List<GoodsUnit>> units() {
        return R.ok(unitMapper.selectList(Wrappers.<GoodsUnit>lambdaQuery().orderByAsc(GoodsUnit::getSort)));
    }

    @Operation(summary = "保存单位")
    @PostMapping("/unit")
    public R<Long> saveUnit(@RequestBody GoodsUnit u) {
        if (u.getId() == null) unitMapper.insert(u);
        else unitMapper.updateById(u);
        return R.ok("保存成功", u.getId());
    }

    @Operation(summary = "删除单位")
    @DeleteMapping("/unit/{id}")
    public R<Void> removeUnit(@PathVariable Long id) {
        unitMapper.deleteById(id);
        return R.ok();
    }
}