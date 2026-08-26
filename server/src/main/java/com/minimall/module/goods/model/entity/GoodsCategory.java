package com.minimall.module.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_category")
public class GoodsCategory extends BaseEntity {

    private String name;
    private Long pid;
    private Integer level;
    private String icon;
    private String image;
    private Integer sort;
    private Integer isShow;
}
