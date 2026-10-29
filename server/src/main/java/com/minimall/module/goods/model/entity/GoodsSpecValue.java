package com.minimall.module.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品规格值（如：黑色 / 白色）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_spec_value")
public class GoodsSpecValue extends BaseEntity {

    private Long goodsId;
    private Long specId;
    private String value;
    private String image;
}
