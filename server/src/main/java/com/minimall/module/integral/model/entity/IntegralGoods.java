package com.minimall.module.integral.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("integral_goods")
public class IntegralGoods extends BaseEntity {
    private String name;
    private String image;
    private Long integral;
    private Integer stock;
    private Integer sales;
    private Integer status;
    private String description;
}
