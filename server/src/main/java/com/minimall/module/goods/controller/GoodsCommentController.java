package com.minimall.module.goods.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.minimall.common.result.R;
import com.minimall.module.goods.model.dto.CommentCreateDTO;
import com.minimall.module.goods.model.vo.GoodsCommentVO;
import com.minimall.module.goods.service.GoodsCommentService;
import com.minimall.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端商品评价
 */
@Tag(name = "C端-商品评价")
@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class GoodsCommentController {

    private final GoodsCommentService commentService;
    private final UserService userService;

    @Operation(summary = "商品评价分页")
    @SaIgnore
    @GetMapping("/goods/{goodsId}/comments")
    public R<IPage<GoodsCommentVO>> comments(@PathVariable Long goodsId,
                                             @RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "5") long size) {
        return R.ok(commentService.pageByGoods(goodsId, page, size));
    }

    @Operation(summary = "收货后发表评价")
    @SaCheckLogin(type = "user")
    @PostMapping("/comment/create")
    public R<Long> create(@Valid @RequestBody CommentCreateDTO dto) {
        return R.ok("评价成功", commentService.create(userService.getCurrentUserId(), dto));
    }
}