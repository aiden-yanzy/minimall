package com.minimall.module.content.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article_category")
public class ArticleCategory extends BaseEntity {
    private String name;
    private Integer sort;
    private Integer status;
}
