package com.minimall.module.seckill.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minimall.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seckill_goods")
public class SeckillGoods extends BaseEntity {
    private Long activityId;
    private Long goodsId;
    private Long goodsItemId;
    private Long seckillPrice;
    private Integer seckillStock;
    private Integer seckillSales;
    private Integer perLimit;
    private Integer sort;
}
