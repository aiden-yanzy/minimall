package com.minimall.module.seckill.model.vo;

import lombok.Data;

@Data
public class SeckillGoodsVO {
    private Long id;
    private Long goodsId;
    private Long goodsItemId;
    private String goodsName;
    private String goodsImage;
    private String specValueStr;
    private Long seckillPrice;
    private Long marketPrice;
    private Integer seckillStock;
    private Integer seckillSales;
    private Integer perLimit;
    private Long remainStock;
}