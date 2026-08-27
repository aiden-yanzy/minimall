package com.minimall.module.goods.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minimall.common.result.R;
import com.minimall.module.goods.model.entity.GoodsComment;
import com.minimall.module.goods.service.GoodsCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台评价管理
 */
@Tag(name = "后台-评价管理")
@RestController
@RequestMapping("/admin/comment")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminCommentController {

    private final GoodsCommentService commentService;

    @Operation(summary = "评价列表")
    @GetMapping
    public R<List<GoodsComment>> list() {
        return R.ok(commentService.list(Wrappers.<GoodsComment>lambdaQuery().orderByDesc(GoodsComment::getId)));
    }

    @Operation(summary = "显示/隐藏评价")
    @PutMapping("/{id}/show")
    public R<Void> toggleShow(@PathVariable Long id, @RequestParam Integer isShow) {
        commentService.lambdaUpdate().eq(GoodsComment::getId, id).set(GoodsComment::getIsShow, isShow).update();
        return R.ok();
    }
}