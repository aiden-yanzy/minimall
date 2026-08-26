package com.minimall.module.content.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minimall.module.content.mapper.ArticleCategoryMapper;
import com.minimall.module.content.mapper.ArticleMapper;
import com.minimall.module.content.model.entity.Article;
import com.minimall.module.content.model.entity.ArticleCategory;
import com.minimall.module.system.mapper.SystemConfigMapper;
import com.minimall.module.system.model.entity.SystemConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 内容：文章 + 首页装修(JSON 配置存 config 表)
 */
@Service
@RequiredArgsConstructor
public class ContentService {

    public static final String HOME_DECORATE = "home_decorate";

    private final ArticleMapper articleMapper;
    private final ArticleCategoryMapper categoryMapper;
    private final SystemConfigMapper configMapper;

    // ---------------- 文章 ----------------

    public List<ArticleCategory> categories() {
        return categoryMapper.selectList(Wrappers.<ArticleCategory>lambdaQuery()
                .eq(ArticleCategory::getStatus, 1).orderByAsc(ArticleCategory::getSort));
    }

    public List<Article> articleList(Long categoryId) {
        return articleMapper.selectList(Wrappers.<Article>lambdaQuery()
                .eq(Article::getIsShow, 1)
                .eq(categoryId != null, Article::getCategoryId, categoryId)
                .orderByAsc(Article::getSort));
    }

    public Article articleDetail(Long id) {
        return articleMapper.selectById(id);
    }

    public void saveCategory(ArticleCategory c) {
        if (c.getId() == null) categoryMapper.insert(c);
        else categoryMapper.updateById(c);
    }

    public void saveArticle(Article a) {
        if (a.getId() == null) articleMapper.insert(a);
        else articleMapper.updateById(a);
    }

    // ---------------- 装修 ----------------

    /** 首页装修 JSON（无配置时返回默认结构） */
    public String getHomeDecorate() {
        SystemConfig cfg = configMapper.selectOne(Wrappers.<SystemConfig>lambdaQuery()
                .eq(SystemConfig::getName, HOME_DECORATE).last("limit 1"));
        if (cfg != null && cfg.getValue() != null) return cfg.getValue();
        // 默认装修
        return JSONUtil.toJsonStr(new DefaultHome());
    }

    public void saveHomeDecorate(String json) {
        SystemConfig cfg = configMapper.selectOne(Wrappers.<SystemConfig>lambdaQuery()
                .eq(SystemConfig::getName, HOME_DECORATE).last("limit 1"));
        if (cfg == null) {
            cfg = new SystemConfig();
            cfg.setName(HOME_DECORATE);
            cfg.setValue(json);
            cfg.setRemark("C端首页装修");
            configMapper.insert(cfg);
        } else {
            configMapper.update(null, Wrappers.<SystemConfig>lambdaUpdate()
                    .eq(SystemConfig::getId, cfg.getId()).set(SystemConfig::getValue, json));
        }
    }

    /** 默认首页：轮播 + 宫格 */
    public static class DefaultHome {
        public java.util.List<Object> banners = List.of(
                java.util.Map.of("image", "", "link", ""),
                java.util.Map.of("image", "", "link", ""));
        public java.util.List<Object> navs = List.of(
                java.util.Map.of("name", "积分商城", "icon", "", "link", "/pages/integral/list"),
                java.util.Map.of("name", "限时秒杀", "icon", "", "link", "/pages/seckill/list"),
                java.util.Map.of("name", "我的拼团", "icon", "", "link", "/pages/team/list"),
                java.util.Map.of("name", "领券中心", "icon", "", "link", "/pages/coupon/center"));
        public String goodsTitle = "推荐好物";
    }
}