package com.minimall.module.content.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article")
public class Article extends BaseEntity {
    private Long categoryId;
    private String title;
    private String image;
    private String content;
    private Integer isShow;
    private Integer sort;
}
