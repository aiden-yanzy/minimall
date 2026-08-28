package com.minimall.module.freight.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_delivery_template")
public class GoodsDeliveryTemplate extends BaseEntity {
    private String name;
    private Integer chargeType;
    private Integer isFreeShipping;
}
