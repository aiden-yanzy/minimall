package com.minimall.module.cart.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 购物车
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cart")
public class Cart extends BaseEntity {

    private Long userId;
    private Long goodsId;
    private Long goodsItemId;
    private String goodsSnapshot;
    private Integer num;
    private Integer isSelected;
}
