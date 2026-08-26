package com.minimall.module.cart.model.vo;

import lombok.Data;

/**
 * 购物车项（含商品信息）
 */
@Data
public class CartItemVO {
    private Long id;
    private Long goodsId;
    private Long goodsItemId;
    private String goodsName;
    private String goodsImage;
    private String specValueStr;
    private Long price;
    private Integer num;
    private Integer isSelected;
    private Long totalPrice;
    private Integer stock;
}
