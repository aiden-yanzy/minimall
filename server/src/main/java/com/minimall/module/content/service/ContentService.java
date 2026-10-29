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
        // 服务端校验：结构不合法直接拒绝，避免把 C 端首页搞挂
        cn.hutool.json.JSONObject obj;
        try {
            obj = JSONUtil.parseObj(json);
        } catch (Exception e) {
            throw new com.minimall.common.exception.BusinessException("装修内容不是合法 JSON");
        }
        cn.hutool.json.JSONArray banners = obj.getJSONArray("banners");
        if (banners == null || banners.isEmpty()) {
            throw new com.minimall.common.exception.BusinessException("至少需要一张轮播图");
        }
        if (banners.size() > 8) {
            throw new com.minimall.common.exception.BusinessException("轮播图最多 8 张");
        }
        for (int i = 0; i < banners.size(); i++) {
            String img = banners.getJSONObject(i).getStr("image", "");
            if (img == null || img.isBlank()) {
                throw new com.minimall.common.exception.BusinessException("第 " + (i + 1) + " 张轮播图缺少图片地址");
            }
        }
        cn.hutool.json.JSONArray navs = obj.getJSONArray("navs");
        if (navs != null) {
            if (navs.size() > 10) {
                throw new com.minimall.common.exception.BusinessException("宫格导航最多 10 个");
            }
            for (int i = 0; i < navs.size(); i++) {
                cn.hutool.json.JSONObject n = navs.getJSONObject(i);
                if (n.getStr("name", "").isBlank()) {
                    throw new com.minimall.common.exception.BusinessException("第 " + (i + 1) + " 个宫格缺少名称");
                }
            }
        }

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