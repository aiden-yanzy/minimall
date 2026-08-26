package com.minimall.module.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品 SKU
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_item")
public class GoodsItem extends BaseEntity {

    private Long goodsId;
    private String itemSn;
    private String image;
    private Long price;
    private Long costPrice;
    private Long marketPrice;
    private Integer stock;
    private Integer sales;
    private BigDecimal weight;
    private BigDecimal volume;
    private String specValueIds;
    private String specValueStr;
    private Integer status;
}
