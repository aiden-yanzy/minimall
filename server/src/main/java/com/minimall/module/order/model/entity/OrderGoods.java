package com.minimall.module.order.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单商品（快照）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_goods")
public class OrderGoods extends BaseEntity {

    private Long orderId;
    private Long userId;
    private Long goodsId;
    private Long goodsItemId;
    private String goodsName;
    private String goodsImage;
    private String specValueStr;
    private Long price;
    private Integer num;
    private Long totalPrice;
    private Long discountPrice;
    private Integer isComment;
    private Integer isAfterSale;
}
