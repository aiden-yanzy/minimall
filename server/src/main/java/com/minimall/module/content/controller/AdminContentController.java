package com.minimall.module.content.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.minimall.common.result.R;
import com.minimall.module.content.model.entity.Article;
import com.minimall.module.content.model.entity.ArticleCategory;
import com.minimall.module.content.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台内容管理：文章/分类 + 首页装修
 */
@Tag(name = "后台-内容/装修")
@RestController
@RequestMapping("/admin/content")
@RequiredArgsConstructor
@SaCheckLogin(type = "admin")
public class AdminContentController {

    private final ContentService contentService;

    @Operation(summary = "首页装修(读取)") 
    @GetMapping("/decorate")
    public R<String> decorate() {
        return R.ok(contentService.getHomeDecorate());
    }

    @Operation(summary = "首页装修(保存 JSON)")
    @cn.dev33.satoken.annotation.SaCheckPermission(value = "content:manage", type = "admin")
    @PostMapping("/decorate")
    public R<Void> saveDecorate(@RequestBody String json) {
        contentService.saveHomeDecorate(json);
        return R.ok("保存成功", null);
    }

    @Operation(summary = "文章分类")
    @GetMapping("/article/categories")
    public R<List<ArticleCategory>> categories() {
        return R.ok(contentService.categories());
    }

    @Operation(summary = "保存文章分类")
    @PostMapping("/article/category")
    public R<Long> saveCategory(@RequestBody ArticleCategory c) {
        contentService.saveCategory(c);
        return R.ok("保存成功", c.getId());
    }

    @Operation(summary = "文章列表")
    @GetMapping("/article/list")
    public R<List<Article>> articles() {
        return R.ok(contentService.articleList(null));
    }

    @Operation(summary = "保存文章")
    @PostMapping("/article")
    public R<Long> saveArticle(@RequestBody Article a) {
        contentService.saveArticle(a);
        return R.ok("保存成功", a.getId());
    }
}