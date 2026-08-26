package com.minimall.module.goods.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.minimall.common.result.R;
import com.minimall.module.goods.model.dto.GoodsSaveDTO;
import com.minimall.module.goods.model.entity.Goods;
import com.minimall.module.goods.model.entity.GoodsCategory;
import com.minimall.module.goods.model.vo.CategoryTreeNode;
import com.minimall.module.goods.service.GoodsCategoryService;
import com.minimall.module.goods.service.GoodsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台商品管理
 */
@Tag(name = "后台-商品管理")
@RestController
@RequestMapping("/admin/goods")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminGoodsController {

    private final GoodsService goodsService;
    private final GoodsCategoryService categoryService;

    @Operation(summary = "商品分页")
    @GetMapping
    public R<IPage<Goods>> list(@RequestParam(defaultValue = "1") long page,
                                @RequestParam(defaultValue = "10") long size,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Integer status) {
        return R.ok(goodsService.adminPage(page, size, keyword, status));
    }

    @Operation(summary = "商品详情(编辑回显)")
    @GetMapping("/{id}")
    public R<Goods> detail(@PathVariable Long id) {
        return R.ok(goodsService.getById(id));
    }

    @Operation(summary = "创建商品")
    @PostMapping
    public R<Long> create(@Valid @RequestBody GoodsSaveDTO dto) {
        return R.ok("创建成功", goodsService.save(dto));
    }

    @Operation(summary = "编辑商品")
    @PutMapping("/{id}")
    public R<Long> update(@PathVariable Long id, @Valid @RequestBody GoodsSaveDTO dto) {
        dto.setId(id);
        return R.ok("保存成功", goodsService.save(dto));
    }

    @Operation(summary = "上下架")
    @PutMapping("/status/{id}")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        goodsService.updateStatus(id, status);
        return R.ok();
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        goodsService.remove(id);
        return R.ok();
    }

    // ---------------- 分类管理 ----------------

    @Operation(summary = "分类树")
    @GetMapping("/category/tree")
    public R<java.util.List<CategoryTreeNode>> categoryTree() {
        return R.ok(categoryService.tree());
    }

    @Operation(summary = "保存分类")
    @PostMapping("/category")
    public R<Long> saveCategory(@RequestBody GoodsCategory category) {
        if (category.getName() == null || category.getName().isBlank()) {
            return R.fail("分类名称不能为空");
        }
        categoryService.saveOrUpdate(category);
        return R.ok("保存成功", category.getId());
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/category/{id}")
    public R<Void> removeCategory(@PathVariable Long id) {
        categoryService.removeById(id);
        return R.ok();
    }
}
