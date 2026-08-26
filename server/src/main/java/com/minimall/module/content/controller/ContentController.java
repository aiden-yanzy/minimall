package com.minimall.module.content.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.minimall.common.result.R;
import com.minimall.module.content.model.entity.Article;
import com.minimall.module.content.model.entity.ArticleCategory;
import com.minimall.module.content.service.ContentService;
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
 * C 端内容：文章 + 首页装修
 */
@Tag(name = "C端-内容/装修")
@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @Operation(summary = "首页装修配置")
    @SaIgnore
    @GetMapping("/decorate/home")
    public R<String> decorate() {
        return R.ok(contentService.getHomeDecorate());
    }

    @Operation(summary = "文章分类")
    @SaIgnore
    @GetMapping("/article/categories")
    public R<List<ArticleCategory>> categories() {
        return R.ok(contentService.categories());
    }

    @Operation(summary = "文章列表")
    @SaIgnore
    @GetMapping("/article/list")
    public R<List<Article>> articleList(@RequestParam(required = false) Long categoryId) {
        return R.ok(contentService.articleList(categoryId));
    }

    @Operation(summary = "文章详情")
    @SaIgnore
    @GetMapping("/article/{id}")
    public R<Article> article(@PathVariable Long id) {
        return R.ok(contentService.articleDetail(id));
    }
}