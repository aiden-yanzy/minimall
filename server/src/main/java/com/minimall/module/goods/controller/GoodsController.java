package com.minimall.module.goods.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.minimall.common.result.R;
import com.minimall.module.goods.model.vo.CategoryTreeNode;
import com.minimall.module.goods.model.vo.GoodsVO;
import com.minimall.module.goods.service.GoodsCategoryService;
import com.minimall.module.goods.service.GoodsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端商品接口（游客可看）
 */
@Tag(name = "C端-商品")
@RestController
@RequestMapping("/shop/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;
    private final GoodsCategoryService categoryService;

    @Operation(summary = "商品分类树")
    @SaIgnore
    @GetMapping("/category")
    public R<List<CategoryTreeNode>> category() {
        return R.ok(categoryService.tree());
    }

    @Operation(summary = "商品分页列表")
    @SaIgnore
    @GetMapping
    public R<IPage<GoodsVO>> list(@RequestParam(defaultValue = "1") long page,
                                  @RequestParam(defaultValue = "10") long size,
                                  @RequestParam(required = false) Long categoryId,
                                  @RequestParam(required = false) String keyword) {
        return R.ok(goodsService.pageForShop(page, size, categoryId, keyword));
    }

    @Operation(summary = "商品详情")
    @SaIgnore
    @GetMapping("/{id}")
    public R<GoodsVO> detail(@PathVariable Long id) {
        return R.ok(goodsService.detail(id));
    }
}
