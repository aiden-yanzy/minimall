package com.minimall.module.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品 SPU
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods")
public class Goods extends BaseEntity {

    private String name;
    private String subtitle;
    private Long categoryId;
    private Long brandId;
    private Long unitId;
    private String image;
    private String video;
    private Long minPrice;
    private Long maxPrice;
    private Long marketPrice;
    private Integer stock;
    private Integer sales;
    private Integer virtualSales;
    private String content;
    private Long deliveryTemplateId;
    private Integer specType;
    private Integer status;
    private Integer recommend;
    private Integer isShow;
    private Integer sort;
    private Integer refundType;
}
