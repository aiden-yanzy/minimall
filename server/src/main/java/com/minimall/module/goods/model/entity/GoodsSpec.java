package com.minimall.module.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品规格组（如：颜色 / 尺寸）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_spec")
public class GoodsSpec extends BaseEntity {

    private Long goodsId;
    private String name;
    private Integer sort;
}
